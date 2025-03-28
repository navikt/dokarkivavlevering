package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import com.google.common.collect.Lists;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlGraphQLConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerService {

	private final SakRepository sakRepository;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public AvsluttAlleSakerService(SakRepository sakRepository, PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.sakRepository = sakRepository;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public void avsluttAlleSaker() {
		//Hent 1000 og 1000
		List<Long> sakIds = sakRepository.findAllSakIds();

		List<List<Long>> sakIdsPartitioned = Lists.partition(sakIds, 1000);

		sakIdsPartitioned.forEach(sakIdListe -> {
			List<Sak> saker = sakRepository.findSaksBySakIdIn(sakIdListe);
			oppdaterIdIDatabase(saker);
		});

	}

	public void oppdaterIdIDatabase(List<Sak> saker) {
		List<String> aktoerId = saker.stream().map(Sak::getAktoerId).toList();
		//Spørr pdl om siste id for alle
		//Oppdater databasen med ny aktoerId/orgnr og status
	}
}
