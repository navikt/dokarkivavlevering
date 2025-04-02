package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SakRepository;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private static final int BATCHSTOERRELSE = 1000;

	private final SakRepository sakRepository;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public AvsluttAlleSakerService(SakRepository sakRepository, PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.sakRepository = sakRepository;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public void avsluttAlleSaker() {
		//Hent 1000 og 1000
		List<Long> sakIds = sakRepository.findAllSakIds();

		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, BATCHSTOERRELSE);

		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Sak> saker = sakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterIdIDatabase(saker);
		});
	}

	// TODO: Tiltak for at både aktørId og orgnr er sett, eller ingen av dei, i ei sak
	private void oppdaterIdIDatabase(List<Sak> saker) {
		List<Sak> sakerUtenAktoerId = saker.stream()
				.filter(sak -> sak.getOrgnr() != null)
				.toList();
		sakerUtenAktoerId.forEach(sak -> sak.setStatus("SKAL_IKKE_HENTE_FRA_PDL"));

		List<Sak> sakerMedAktoerId = saker.stream()
				.filter(sak -> sak.getAktoerId() != null)
				.toList();

		Set<String> aktoerIds = sakerMedAktoerId.stream()
				.map(Sak::getAktoerId)
				.collect(Collectors.toSet());

		if (!aktoerIds.isEmpty()) {
			oppdaterSakerMedAktoerId(aktoerIds, sakerMedAktoerId);
		}
	}

	private void oppdaterSakerMedAktoerId(Set<String> aktoerIds, List<Sak> sakerMedAktoerId) {
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
				sak.setStatus("PDL_FANT_IKKE_NY_AKTOERID");
			} else {
				if (aktoerIderSomSkalOppdateres.containsKey(sak.getAktoerId())) {
					sak.setAktoerId(aktoerIderSomSkalOppdateres.get(sak.getAktoerId()));
				}
				sak.setStatus("HENTET_FRA_PDL");
			}
		});
	}
}
