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
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_PDL_FANT_IKKE_AKTOERID;
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

	//TODO: Rydd opp i denne klassen. Den er et work in progress
	public void avsluttAlleSaker() {
		//Oppdater alle aktoerId'er til siste gyldige
		oppdaterAlleAktoerIder();

		avsluttAlleSakerForAktoerId();

		avsluttAlleSakerOrgnr();
	}

	private void avsluttAlleSakerForAktoerId(){
		List<String> alleAktoerIder = arbeidssakRepository.findDistinctAktoerIds(ENDELIGE_STATUSER);
		//Del alle aktørId'ene opp i håndterlige partisjoner
		List<List<String>> aktoerIdsPartitioned = Lists.partition(alleAktoerIder, 200);

		for (List<String> aktoerIdList : aktoerIdsPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByAktoerIdIn(aktoerIdList);

			//Grupper arbeidssakene til arkivsaker
			List<List<Arbeidssak>> groupedLists = new ArrayList<>(
					arbeidssaker.stream()
							.collect(Collectors.groupingBy(arbeidssak -> List.of(
									arbeidssak.getAktoerId(),
									arbeidssak.getApplikasjon(),
									//TODO: Ikke grupper de som mangler fagsaknr
									arbeidssak.getFagsaknr() == null ? "" : arbeidssak.getFagsaknr()
							)))
							.values()
			);

			List<Arkivsak> arkivsaker = createArkivsaker(groupedLists);
			arkivsaker.forEach(this::avsluttSak);
		}
	}

	private void avsluttAlleSakerOrgnr(){
		List<String> alleOrgNr = arbeidssakRepository.findDistinctOrgnrs(ENDELIGE_STATUSER);
		List<List<String>> orgnrPartitioned = Lists.partition(alleOrgNr, 200);

		for (List<String> orgNrList : orgnrPartitioned) {
			//Finn alle tilhørende arbeidssaker for aktørId'ene i partisjonen
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksByOrgnrIn(orgNrList);

			List<List<Arbeidssak>> groupedLists = new ArrayList<>(
					arbeidssaker.stream()
							.collect(Collectors.groupingBy(arbeidssak -> List.of(
									arbeidssak.getOrgnr(),
									arbeidssak.getApplikasjon(),
									//TODO: Ikke grupper de som mangler fagsaknr
									arbeidssak.getFagsaknr() == null ? "" : arbeidssak.getFagsaknr()
							)))
							.values()
			);

			List<Arkivsak> arkivsaker = createArkivsaker(groupedLists);
			arkivsaker.forEach(this::avsluttSak);
		}
	}

	private List<Arkivsak> createArkivsaker(List<List<Arbeidssak>> arbeidssaksListe) {
		ArrayList<Arkivsak> arkivsaker = new ArrayList<>();
		for (List<Arbeidssak> arbsaker : arbeidssaksListe) {
			Arkivsak arkivsak = new Arkivsak(arbsaker, new ArrayList<>());
			List<Journalpost> tilhoerendeJournalposter = avsluttSakRepository.getJournalposterForArkivsak(arkivsak.getArbeidssaksIder());
			arkivsak.journalposter().addAll(tilhoerendeJournalposter);
			arkivsaker.add(arkivsak);
		}
		return arkivsaker;
	}

	private void avsluttSak(Arkivsak arkivsak) {
		try {
			validerArkivsakHarIngenAapneJournalposter(arkivsak);
			hvisTomArkivsak_avsluttBehandlingOgAvbrytSak(arkivsak);

			Journalpost eldsteJournalpost = finnEldsteJournalpostForArkivsak(arkivsak);

			String administrativEnhet = avsluttSakProperties.getAdministrativEnhet();
			if (isEmpty(administrativEnhet)) {
				administrativEnhet = hentHistoriskNavnForAdminEnhet(eldsteJournalpost, arkivsak);
			}

			LocalDateTime datoSakOpprettet = eldsteJournalpost.getOpprettetdato();
			avsluttSakRepository.avsluttSaker(arkivsak.getArbeidssaksIder(), avsluttSakProperties.getAvsluttetDato(), datoSakOpprettet, administrativEnhet);
			oppdaterArbeidsstatusForArkivsak(arkivsak, FERDIG_SAK_AVSLUTTET);

		} catch (KanIkkeBehandleArkivsakException e) {
			log.warn(e.getMessage());
		}
	}

	private void oppdaterAlleAktoerIder() {
		List<Long> sakIds = arbeidssakRepository.findAllSakIdsWhereStatusIsNullOrAapen();
		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, BATCHSTOERRELSE);

		// Oppdater alle aktoerIder
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterAlleAktoerIder(arbeidssaker);
		});
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

	private void oppdaterAlleAktoerIder(List<Arbeidssak> saker) {
		List<Arbeidssak> sakerMedOrgnr = saker.stream()
				.filter(arbeidssak -> arbeidssak.getOrgnr() != null)
				.toList();
		sakerMedOrgnr.forEach(arbeidssak -> arbeidssak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL));

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

	private void oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(Set<String> aktoerIds, List<Arbeidssak> arbeidssakerMedAktoerId) {
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
				arbeidssak.setArbeidsstatus(FEIL_PDL_FANT_IKKE_AKTOERID);
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(arbeidssak.getAktoerId())) {
					arbeidssak.setAktoerId(aktoerIderSomSkalOppdateres.get(arbeidssak.getAktoerId()));
				}
				arbeidssak.setArbeidsstatus(HENTET_FRA_PDL);
			}
		});
	}

	private void oppdaterArbeidsstatusForArkivsak(Arkivsak arkivsak, Arbeidsstatus arbeidsstatus) {
		//TODO: Ved oppdatering til en endelig status burde vi endre vanlig status også, ikke bare arbeidsstatus
		arkivsak.arbeidssaker().forEach(arbeidssak -> arbeidssak.setArbeidsstatus(arbeidsstatus));
	}
}
