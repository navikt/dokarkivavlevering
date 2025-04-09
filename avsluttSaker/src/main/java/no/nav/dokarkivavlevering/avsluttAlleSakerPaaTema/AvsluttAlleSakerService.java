package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusConsumer;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers.DatavarehusResponse;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Arbeidssak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.AvsluttSakRepository;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_AAPEN_JOURNALPOST;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FEIL_ADMINISTRATIV_ENHET_MANGLER;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.FERDIG_TOM_ARKIVSAK;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.HENTET_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PDL_FANT_IKKE_NY_AKTOERID;
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

	private static final int BATCHSTOERRELSE = 1000;

	private final ArbeidssakRepository arbeidssakRepository;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final AvsluttSakRepository avsluttSakRepository;
	private final AvsluttSakProperties avsluttSakProperties;
	private final DatavarehusConsumer datavarehusConsumer;

	public AvsluttAlleSakerService(ArbeidssakRepository arbeidssakRepository,
								   PdlGraphQLConsumer pdlGraphQLConsumer,
								   AvsluttSakRepository avsluttSakRepository,
								   AvsluttSakProperties avsluttSakProperties,
								   DatavarehusConsumer datavarehusConsumer) {
		this.arbeidssakRepository = arbeidssakRepository;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.avsluttSakRepository = avsluttSakRepository;
		this.avsluttSakProperties = avsluttSakProperties;
		this.datavarehusConsumer = datavarehusConsumer;
	}

	public void avsluttAlleSaker() {
		List<Long> sakIds = arbeidssakRepository.findAllSakIds();
		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, BATCHSTOERRELSE);

		// Oppdater aktoerIder
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterAktoerIder(arbeidssaker);
		});

		// Finn arkivsaker
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Arbeidssak> arbeidssaker = arbeidssakRepository.findSaksBySakIdIn(sakIdListe);
			genererArkivsak(arbeidssaker);
		});
	}

	private void genererArkivsak(List<Arbeidssak> arbeidssaker) {

		for (Arbeidssak arbeidssak : arbeidssaker) {
			List<Arbeidssak> tilhoerendeArbeidssaker = hentTilhoerendeArbeidssakerForArbeidssak(arbeidssak);
			List<Long> saksIder = tilhoerendeArbeidssaker.stream().map(Arbeidssak::getSakId).toList();

			List<Journalpost> tilhoerendeJournalposter = avsluttSakRepository.getJournalposterForArkivsak(saksIder);
			Arkivsak arkivsak = new Arkivsak(tilhoerendeArbeidssaker, tilhoerendeJournalposter);
			if (harArkivsakEnAapenJournalpost(arkivsak.journalposter())) {
				log.warn("Kan ikke avslutte arkivsak med åpne journalposter for saksIder={}", saksIder);
				arkivsak.arbeidssaker().forEach(tmpArbeidssak -> tmpArbeidssak.setArbeidsstatus(FEIL_AAPEN_JOURNALPOST.name()));
				return;
			}

			if (!harArkivsakFerdigstilteJournalposter(arkivsak.journalposter())) {
				log.info("Arkivsak har ingen ferdigstilte journalposter. Avbryter saker={} knyttet til tom arkivsak.", saksIder);
				avsluttSakRepository.updateSakForArkivsak(arkivsak.getArbeidssaksIder());
				arkivsak.arbeidssaker().forEach(tmpArbeidssak -> tmpArbeidssak.setArbeidsstatus(FERDIG_TOM_ARKIVSAK.name()));
				return;
			}

			Optional<Journalpost> eldsteJournalpostOptional = finnEldsteJournalpost(arkivsak);
			if (eldsteJournalpostOptional.isEmpty()) {
				log.warn("Fant ingen administrativ enhet for arkivsak med saksIder={}", saksIder);
				arkivsak.arbeidssaker().forEach(tmpArbeidssak -> tmpArbeidssak.setArbeidsstatus(FEIL_ADMINISTRATIV_ENHET_MANGLER.name()));
				return;
			}

			//Hvis input.administrativEnhet ikke er satt så hent navnet journalførende enhet hadde når eldste journalpost (fra steg 3.2) ble journalført.
			Journalpost eldsteJournalpost = eldsteJournalpostOptional.get();
			determineJournalfoerendeEnhet(eldsteJournalpost);

			//3.3
			//Finn administrativ enhet

			//3.4
			//oppdater sak
			//arkivsakForSak.forEach(tmpSak -> tmpSak.setArbeidsstatus(SAK_AVSLUTTET.name()));
		}
		//Håndtere arkivsaken - da blir det 1 og en

		//Finne alle arkivsaker og håndetere bolker

	}

	/*
	Hvis input.administrativEnhet ikke er satt så hent navnet journalførende enhet hadde når eldste journalpost (fra steg 3.2) ble journalført.
	 */

	/*
	HVIS ingen kontortyper (ARENAENHET, INFOENHET, NORGENHET) returneres SÅ skriv feilmelding til logg (kan ikke finne navn på administrativ enhet) og gå til neste arkivsak
ELLERS HVIS kun en kontortype (ARENAENHET, INFOENHET, NORGENHET) returneres SÅ sett administrativ enhet = nav_enhet_navn fra denne kontortypen
ELLERS HVIS sak.applikasjon = "IT01" og kontortype INFOENHET returneres SÅ sett administrativ enhet = nav_enhet_navn fra kontortype INFOENHET
ELLERS HVIS sak.applikasjon = "AO01" og kontortype ARENAENHET returneres SÅ sett administrativ enhet = nav_enhet_navn fra kontortype ARENAENHET
ELLERS HVIS kontortype NORGENHET returneres SÅ sett administrativ enhet = nav_enhet_navn fra kontortype NORGENHET
ELLERS HVIS kontortype NORGENHET ikke er returnert og kontortype INFOENHET er returnert SÅ  sett administrativ enhet = nav_enhet_navn fra kontortype INFOENHET
ELLERS HVIS kontortype NORGENHET ikke er returnert og kontortype ARENAENHET er returnert så sett administrativ enhet = nav_enhet_navn fra kontortype ARENAENHET
ELLERS skriv feilmelding til logg (kan ikke finne navn på administrativ enhet) og gå til neste arkivsak
	 */
	private String determineJournalfoerendeEnhet(Journalpost eldsteJournalpost) {
		if(!isEmpty(avsluttSakProperties.getAdministrativEnhet())) {
			return avsluttSakProperties.getAdministrativEnhet();
		} else {
			datavarehusConsumer.hentNavnForAdministrativEnhet(eldsteJournalpost.getJournalfoerendeEnhet(), eldsteJournalpost.getJournaldato());

			return null;
		}
	}

	private Optional<Journalpost> finnEldsteJournalpost(Arkivsak arkivsak) {
		String inputAdministrativEnhet = avsluttSakProperties.getAdministrativEnhet();
		List<Journalpost> filtrerteJournalposter = arkivsak.journalposter().stream()
				.filter(journalpost -> LUKKEDE_JOURNALSTATUSER.contains(journalpost.getJournalstatus()))
				.filter(journalpost -> !journalpost.isErFeilregistrert())
				.filter(journalpost -> !isEmpty(inputAdministrativEnhet) || (!isEmpty(journalpost.getJournalfoerendeEnhet()) && !"9999".equals(journalpost.getJournalfoerendeEnhet())))
				.toList();

		if(filtrerteJournalposter.isEmpty()) {
			return Optional.empty();
		}
		return filtrerteJournalposter.stream().min(Comparator.comparing(Journalpost::getJournaldato));

	}

		/*List<Sak> arkivsak = sakRepository.findArkivSakForAktoerId(sak.getAktoerId(), sak.getFagsaknr(), sak.getApplikasjon());
		Arkivsak A = new Arkivsak(arkivsak);

				.filter(journalpost -> LUKKEDE_JOURNALSTATUSER.contains(journalpost.getJournalstatus()))
		arkivsak.stream().forEach(handleSak -> {
			handleSak.setArkivsak(String.valueOf(UUID.randomUUID()));
			handleSak.setArbeidsStatus("HAR_ARKIVSAK");
		});*/


	private List<Arbeidssak> hentTilhoerendeArbeidssakerForArbeidssak(Arbeidssak arbeidssak) {
		if (arbeidssak.getArbeidsstatus().equals(HENTET_FRA_PDL.name()) || arbeidssak.getArbeidsstatus().equals(PROSESSERING_AV_ARKIVSAK_STARTET.name())) {
			List<Arbeidssak> arkivsakForArbeidssak;
			if (arbeidssak.getFagsaknr() == null) {
				arkivsakForArbeidssak = arbeidssakRepository.findArkivsakForAktoerIdWhereFagsaknrIsNull(arbeidssak.getAktoerId(), arbeidssak.getApplikasjon());
			} else {
				arkivsakForArbeidssak = arbeidssakRepository.findArkivsakForAktoerId(arbeidssak.getAktoerId(), arbeidssak.getFagsaknr(), arbeidssak.getApplikasjon());
			}
			arkivsakForArbeidssak.forEach(tmpArbeidssak -> tmpArbeidssak.setArbeidsstatus(PROSESSERING_AV_ARKIVSAK_STARTET.name()));
			return arkivsakForArbeidssak;
		}
		return emptyList();
	}


	/*
	value = """
			select sr.feilregistrert as erFeilregistrert,
			jp.journalpostStatus as journalstatus,
			jp.journalf_enhet as journalfoerendeEnhet,
			jp.dato_journal as journaldato
			FROM t_saksrelasjon sr
			join t_journalpost jp on jp.journalpost_id = sr.journalpost_id
			where sr.sak_id in (:sakIds)
			"""
	 */

	// TODO: Tiltak for at både aktørId og orgnr er sett, eller ingen av dei, i ei sak
	private void oppdaterAktoerIder(List<Arbeidssak> saker) {
		List<Arbeidssak> sakerUtenAktoerId = saker.stream()
				.filter(arbeidssak -> arbeidssak.getOrgnr() != null)
				.toList();
		sakerUtenAktoerId.forEach(arbeidssak -> arbeidssak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL.name()));

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
			if (isNull(identBolk.getIdenter())) { // 404 Not Found på aktørId, ev. andre feil
				aktoerIderUtenGyldigAktoerId.add(identBolk.getIdent());
			} else {
				// key er gammel aktoerId, value er ny aktoerId
				String gammelAktoerId = identBolk.getIdent();
				String nyAktoerId = identBolk.getIdenter().getFirst().getIdent();

				if (!gammelAktoerId.equals(nyAktoerId)) {
					aktoerIderSomSkalOppdateres.put(identBolk.getIdent(), identBolk.getIdenter().getFirst().getIdent());
				}
			}
		});

		arbeidssakerMedAktoerId.forEach(arbeidssak -> {
			if (aktoerIderUtenGyldigAktoerId.contains(arbeidssak.getAktoerId())) {
				log.warn("Feil ved uthenting av person fra pdl. Sak={}", arbeidssak.getSakId());
				arbeidssak.setArbeidsstatus(PDL_FANT_IKKE_NY_AKTOERID.name());
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(arbeidssak.getAktoerId())) {
					arbeidssak.setAktoerId(aktoerIderSomSkalOppdateres.get(arbeidssak.getAktoerId()));
				}
				arbeidssak.setArbeidsstatus(HENTET_FRA_PDL.name());
			}
		});
	}
}
