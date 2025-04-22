package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.exeptions.KanIkkeBehandleArkivsakException;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.ENDELIGE_STATUSER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_PDL_FANT_IKKE_NY_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FERDIG_SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.HENTET_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PROSESSERING_AV_ARKIVSAK_STARTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.SKAL_IKKE_HENTE_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.ArkivsakValidator.LUKKEDE_JOURNALSTATUSER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.ArkivsakValidator.harArkivsakEnAapenJournalpost;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.ArkivsakValidator.harArkivsakFerdigstilteJournalposter;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private static final String OK = "ok";
	private static final int BATCHSTOERRELSE = 1000;

	private final ArbeidssakRepository arbeidssakRepository;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final AvsluttSakRepository avsluttSakRepository;
	private final AvsluttSakProperties avsluttSakProperties;
	private final AdministrativEnhetService administrativEnhetService;

	public AvsluttAlleSakerService(ArbeidssakRepository arbeidssakRepository,
								   PdlGraphQLConsumer pdlGraphQLConsumer,
								   AvsluttSakRepository avsluttSakRepository,
								   AvsluttSakProperties avsluttSakProperties,
								   AdministrativEnhetService administrativEnhetService) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.avsluttSakRepository = avsluttSakRepository;
		this.avsluttSakProperties = avsluttSakProperties;
		this.administrativEnhetService = administrativEnhetService;
	}

	public void avsluttAlleSaker() {
		List<Long> sakIds = arbeidssakRepository.findAllSakIds();
		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, BATCHSTOERRELSE);

		// Oppdater alle aktoerIder
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterAktoerIder(arbeidssaker);
		});

		// Finn arkivsaker
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			behandleSaker(arbeidssaker);
		});
	}

	private void behandleSaker(List<Arbeidssak> arbeidssaker) {

		for (Arbeidssak arbeidssak : arbeidssaker) {
			if (ENDELIGE_STATUSER.contains(arbeidssak.getArbeidsstatus())) {
				//Hvis arkivsaken allerede er håndtert
				//Dette kan skje hvis flere arbeidssaker i lista som kommer inn tilhører samme arkivsak
				return;
			}
			Arkivsak arkivsak = finnArkivsakForArbeidssak(arbeidssak);

			try {
				validerArkivsakHarIngenAapneJournalposter(arkivsak);
				hvisTomArkivsak_avsluttBehandlingOgAvbrytSak(arkivsak);

				Journalpost eldsteJournalpost = finnEldsteJournalpostForArkivsak(arkivsak);

				String administrativEnhet = avsluttSakProperties.getAdministrativEnhet();
				if (isEmpty(administrativEnhet)) {
					administrativEnhet = hentHistoriskNavnForAdminEnhet(eldsteJournalpost, arkivsak);
				}

				LocalDateTime datoAvsluttet = avsluttSakProperties.getAvsluttetDato() != null ? avsluttSakProperties.getAvsluttetDato() : now();
				LocalDateTime datoSakOpprettet = eldsteJournalpost.getOpprettetdato();
				avsluttSakRepository.avsluttSaker(arkivsak.getArbeidssaksIder(), datoAvsluttet, datoSakOpprettet, administrativEnhet);
				oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_SAK_AVSLUTTET);

			} catch (KanIkkeBehandleArkivsakException e) {
				log.warn(e.getMessage());
			}
		}
	}

	private Arkivsak finnArkivsakForArbeidssak(Arbeidssak arbeidssak) {
		List<Arbeidssak> tilhoerendeArbeidssaker = finnArbeidssakerForArkivsak(arbeidssak);
		List<Long> saksIder = tilhoerendeArbeidssaker.stream()
				.map(Arbeidssak::getSakId)
				.toList();

		List<Journalpost> tilhoerendeJournalposter = avsluttSakRepository.getJournalposterForArkivsak(saksIder);

		return new Arkivsak(tilhoerendeArbeidssaker, tilhoerendeJournalposter);
	}

	private String hentHistoriskNavnForAdminEnhet(Journalpost eldsteJournalpost, Arkivsak arkivsak) {
		Optional<String> administrativEnhetOptional = administrativEnhetService.hentHistoriskNavnForAdministrativEnhet(
				eldsteJournalpost.getJournalfoerendeEnhet(), eldsteJournalpost.getJournaldato(), arkivsak.getApplikasjon());

		if (administrativEnhetOptional.isEmpty()) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_INGEN_ADMINISTRATIV_ENHET_FUNNET_FOR_ARKIVSAK);
			throw new KanIkkeBehandleArkivsakException(format("Fant ingen administrativ enhet for arkivsak med saksIder=%s", arkivsak.getArbeidssaksIder()));
		}
		return administrativEnhetOptional.get();
	}

	private void hvisTomArkivsak_avsluttBehandlingOgAvbrytSak(Arkivsak arkivsak) {
		if (!harArkivsakFerdigstilteJournalposter(arkivsak.journalposter())) {
			avsluttSakRepository.avbrytSaker(arkivsak.getArbeidssaksIder());
			oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_TOM_ARKIVSAK);
			throw new KanIkkeBehandleArkivsakException(format("Arkivsak har ingen ferdigstilte journalposter. Avbryter saker=%s knyttet til tom arkivsak.", arkivsak.getArbeidssaksIder()));
		}
	}

	private void validerArkivsakHarIngenAapneJournalposter(Arkivsak arkivsak) {
		if (harArkivsakEnAapenJournalpost(arkivsak.journalposter())) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_AAPEN_JOURNALPOST);
			throw new KanIkkeBehandleArkivsakException(format("Kan ikke avslutte arkivsak med åpne journalposter for saksIder=%s", arkivsak.getArbeidssaksIder()));
		}
	}

	private Journalpost finnEldsteJournalpostForArkivsak(Arkivsak arkivsak) {
		Optional<Journalpost> eldsteJournalpostOptional = finnEldsteJournalpost(arkivsak);
		if (eldsteJournalpostOptional.isEmpty()) {
			oppdaterArbeidsstatusForArkivsak(arkivsak, FEIL_INGEN_JPER_I_GYLDIG_STATUS_MED_JFR_ENHET);
			throw new KanIkkeBehandleArkivsakException(format("Fant ingen journalposter i gyldig status med journalforendeEnhet for saksIder=%s. Kan ikke bestemme administrativEnhet.", arkivsak.getArbeidssaksIder()));
		}
		return eldsteJournalpostOptional.get();
	}

	private Optional<Journalpost> finnEldsteJournalpost(Arkivsak arkivsak) {
		String inputAdministrativEnhet = avsluttSakProperties.getAdministrativEnhet();
		List<Journalpost> filtrerteJournalposter = arkivsak.journalposter().stream()
				.filter(journalpost -> LUKKEDE_JOURNALSTATUSER.contains(journalpost.getJournalstatus()))
				.filter(journalpost -> !journalpost.isErFeilregistrert())
				.filter(journalpost -> !isEmpty(inputAdministrativEnhet) || (!isEmpty(journalpost.getJournalfoerendeEnhet()) && !"9999".equals(journalpost.getJournalfoerendeEnhet())))
				.toList();

		if (filtrerteJournalposter.isEmpty()) {
			return Optional.empty();
		}
		return filtrerteJournalposter.stream().min(Comparator.comparing(Journalpost::getJournaldato));

	}

	private List<Arbeidssak> finnArbeidssakerForArkivsak(Arbeidssak arbeidssak) {
		List<Arbeidssak> arbeidssakerForArkivsak;
		if (arbeidssak.getFagsaknr() == null) {
			arbeidssakerForArkivsak = arbeidssakRepository.findArkivsakForAktoerIdWhereFagsaknrIsNull(arbeidssak.getAktoerId(), arbeidssak.getApplikasjon());
		} else {
			arbeidssakerForArkivsak = arbeidssakRepository.findArkivsakForAktoerId(arbeidssak.getAktoerId(), arbeidssak.getFagsaknr(), arbeidssak.getApplikasjon());
		}
		oppdaterArbeidsstatusForArbeidssak(arbeidssakerForArkivsak, PROSESSERING_AV_ARKIVSAK_STARTET);
		return arbeidssakerForArkivsak;
	}

	// TODO: Tiltak for at både aktørId og orgnr er sett, eller ingen av dei, i ei sak
	private void oppdaterAktoerIder(List<Arbeidssak> saker) {
		List<Arbeidssak> sakerUtenAktoerId = saker.stream()
				.filter(arbeidssak -> arbeidssak.getOrgnr() != null)
				.toList();
		sakerUtenAktoerId.forEach(arbeidssak -> arbeidssak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL));

		List<Arbeidssak> sakerMedAktoerId = saker.stream()
				.filter(arbeidssak -> arbeidssak.getAktoerId() != null)
				.toList();

		Set<String> aktoerIds = sakerMedAktoerId.stream()
				.map(Arbeidssak::getAktoerId)
				.collect(Collectors.toSet());

		if (!aktoerIds.isEmpty()) {
			oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(aktoerIds, sakerMedAktoerId);
		}
	}

	private void oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl
			(Set<String> aktoerIds, List<Arbeidssak> arbeidssakerMedAktoerId) {
		List<HentIdenterBolk> hentIdenterBolkListe = pdlGraphQLConsumer.hentGjeldendeAktoerIder(aktoerIds);
		Map<String, String> aktoerIderSomSkalOppdateres = new HashMap<>();
		List<String> aktoerIderUtenGyldigAktoerId = new ArrayList<>();

		hentIdenterBolkListe.forEach(identBolk -> {
			if (OK.equals(identBolk.getCode())) {
				// key er gammel aktoerId, value er ny aktoerId
				String gammelAktoerId = identBolk.getIdent();
				String nyAktoerId = identBolk.getIdenter().getFirst().getIdent();

				if (!gammelAktoerId.equals(nyAktoerId)) {
					aktoerIderSomSkalOppdateres.put(identBolk.getIdent(), identBolk.getIdenter().getFirst().getIdent());
				}
			} else {
				aktoerIderUtenGyldigAktoerId.add(identBolk.getIdent());
			}
		});

		arbeidssakerMedAktoerId.forEach(arbeidssak -> {
			if (aktoerIderUtenGyldigAktoerId.contains(arbeidssak.getAktoerId())) {
				log.warn("Feil ved uthenting av person fra pdl. Sak={}", arbeidssak.getSakId());
				arbeidssak.setArbeidsstatus(FEIL_PDL_FANT_IKKE_NY_AKTOERID);
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(arbeidssak.getAktoerId())) {
					arbeidssak.setAktoerId(aktoerIderSomSkalOppdateres.get(arbeidssak.getAktoerId()));
				}
				arbeidssak.setArbeidsstatus(HENTET_FRA_PDL);
			}
		});
	}

	private void oppdaterArbeidsstatusForArkivsak(Arkivsak arkivsak, Arbeidsstatus arbeidsstatus) {
		arkivsak.arbeidssaker().forEach(arbeidssak -> arbeidssak.setArbeidsstatus(arbeidsstatus));
	}

	private void oppdaterArbeidsstatusForArbeidssak(List<Arbeidssak> arbeidssaker, Arbeidsstatus arbeidsstatus) {
		arbeidssaker.forEach(tmpArbeidssak -> tmpArbeidssak.setArbeidsstatus(arbeidsstatus));
	}
}
