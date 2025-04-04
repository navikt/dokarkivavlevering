package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.JournalpostRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SakRepository;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.HENTET_FRA_PDL;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PDL_FANT_IKKE_NY_AKTOERID;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.PROSESSERING_AV_ARKIVSAK_STARTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.SAK_AVSLUTTET;
import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Arbeidsstatus.SKAL_IKKE_HENTE_FRA_PDL;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private static final int BATCHSTOERRELSE = 1000;

	private final SakRepository sakRepository;
	private final JournalpostRepository journalpostRepository;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public AvsluttAlleSakerService(SakRepository sakRepository,
								   JournalpostRepository journalpostRepository,
								   PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.sakRepository = sakRepository;
		this.journalpostRepository = journalpostRepository;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public void avsluttAlleSaker() {
		List<Long> sakIds = sakRepository.findAllSakIds();
		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, BATCHSTOERRELSE);

		// Oppdater aktoerIder
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Sak> saker = sakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterAktoerIder(saker);
		});

		// Finn arkivsaker
		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Sak> saker = sakRepository.findSaksBySakIdIn(sakIdListe);
			settSammenArkivsak2(saker);
		});
	}

	//	Samme applikasjon, aktoerId/orgNr og evt. fagsaknr
	private void settSammenArkivsak2(List<Sak> saker) {

		for (Sak sak : saker) {
			if (sak.getArbeidsstatus().equals(HENTET_FRA_PDL.name()) || sak.getArbeidsstatus().equals(PROSESSERING_AV_ARKIVSAK_STARTET.name())) {
				List<Sak> arkivsakForSak = sakRepository.findArkivsakForAktoerId(sak.getAktoerId(), sak.getFagsaknr(), sak.getApplikasjon());
				arkivsakForSak.forEach(tmpSak -> tmpSak.setArbeidsstatus(PROSESSERING_AV_ARKIVSAK_STARTET.name()));

				//3.1
				//Finn alle journalposter for arkivsaken
				//valider statuser

				//3.1.1
				//Hvis tom arkivsak: Opdater

				//3.2
				//Finn eldste journalpost
				//Hvis ingen journalpost i riktig status, skriv feilmelding og oppdater status

				//3.3
				//Finn administrativ enhet

				//3.4
				//oppdater sak
				//arkivsakForSak.forEach(tmpSak -> tmpSak.setArbeidsstatus(SAK_AVSLUTTET.name()));
			}
			//Håndtere arkivsaken - da blir det 1 og en

			//Finne alle arkivsaker og håndetere bolker

		}

		/*List<Sak> arkivsak = sakRepository.findArkivSakForAktoerId(sak.getAktoerId(), sak.getFagsaknr(), sak.getApplikasjon());
		Arkivsak A = new Arkivsak(arkivsak);

		arkivsak.stream().forEach(handleSak -> {
			handleSak.setArkivsak(String.valueOf(UUID.randomUUID()));
			handleSak.setArbeidsStatus("HAR_ARKIVSAK");
		});*/

	}

	//	Samme applikasjon, aktoerId/orgNr og evt. fagsaknr
	private void settSammenArkivsak() {
		Set<String> aktoerIds = sakRepository.findAllAktoerIds();

		for (String aktoerId : aktoerIds) {
			List<Sak> sakForAktoerId = sakRepository.findSaksByAktoerId(aktoerId);
		}

		/*List<Sak> arkivsak = sakRepository.findArkivSakForAktoerId(sak.getAktoerId(), sak.getFagsaknr(), sak.getApplikasjon());
		Arkivsak A = new Arkivsak(arkivsak);

		arkivsak.stream().forEach(handleSak -> {
			handleSak.setArkivsak(String.valueOf(UUID.randomUUID()));
			handleSak.setStatus("HAR_ARKIVSAK");
		});*/

	}


	// TODO: Tiltak for at både aktørId og orgnr er sett, eller ingen av dei, i ei sak
	private void oppdaterAktoerIder(List<Sak> saker) {
		List<Sak> sakerUtenAktoerId = saker.stream()
				.filter(sak -> sak.getOrgnr() != null)
				.toList();
		sakerUtenAktoerId.forEach(sak -> sak.setArbeidsstatus(SKAL_IKKE_HENTE_FRA_PDL.name()));

		List<Sak> sakerMedAktoerId = saker.stream()
				.filter(sak -> sak.getAktoerId() != null)
				.toList();

		Set<String> aktoerIds = sakerMedAktoerId.stream()
				.map(Sak::getAktoerId)
				.collect(Collectors.toSet());

		if (!aktoerIds.isEmpty()) {
			oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(aktoerIds, sakerMedAktoerId);
		}
	}

	private void oppdaterArbeidssakMedGjeldendeAktoerIdFraPdl(Set<String> aktoerIds, List<Sak> sakerMedAktoerId) {
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

		sakerMedAktoerId.forEach(sak -> {
			if (aktoerIderUtenGyldigAktoerId.contains(sak.getAktoerId())) {
				log.warn("Feil ved uthenting av person fra pdl. Sak={}", sak.getSakId());
				sak.setArbeidsstatus(PDL_FANT_IKKE_NY_AKTOERID.name());
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(sak.getAktoerId())) {
					sak.setAktoerId(aktoerIderSomSkalOppdateres.get(sak.getAktoerId()));
				}
				sak.setArbeidsstatus(HENTET_FRA_PDL.name());
			}
		});
	}
}
