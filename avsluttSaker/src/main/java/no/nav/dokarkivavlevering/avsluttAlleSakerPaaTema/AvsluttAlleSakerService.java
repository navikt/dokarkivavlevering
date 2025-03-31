package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlHentIdenterBolkResponse;
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

	public void oppdaterIdIDatabase(List<Sak> saker) {
		Set<String> aktoerIds = saker.stream().map(Sak::getAktoerId).filter(Objects::nonNull).collect(Collectors.toSet());
		List<PdlHentIdenterBolkResponse.PdlHentIdenterBolk> pdlHentIdenterBolk = pdlGraphQLConsumer.hentGjeldendeAktoerIdForBolk(aktoerIds);
		Map<String, String> aktoerIdMap = new HashMap<>();
		List<String> badAktoerIds = new ArrayList<>();

		pdlHentIdenterBolk.forEach(identBolk -> {
			if (isNull(identBolk.getIdenter())) {
				badAktoerIds.add(identBolk.getIdent());
			} else {
				aktoerIdMap.put(identBolk.getIdent(), identBolk.getIdenter().getFirst().getIdent());
			}
		});

		saker.forEach(sak -> {
			if (badAktoerIds.contains(sak.getAktoerId())) {
				log.warn("Feil ved uthenting av person fra pdl. Sak={}", sak.getSakId());
				sak.setStatus("FEIL_FRA_PDL");
			} else {
				sak.setAktoerId(aktoerIdMap.get(sak.getAktoerId()));
				sak.setStatus("HENTET_FRA_PDL");
			}
		});
	}
}
