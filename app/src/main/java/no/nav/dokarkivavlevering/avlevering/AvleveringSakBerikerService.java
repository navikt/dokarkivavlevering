package no.nav.dokarkivavlevering.avlevering;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.ExchangeProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringSakBerikerService {

	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public AvleveringSakBerikerService(PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public List<Sak> berikSaker(final List<Sak> saker, @ExchangeProperty(AvleveringRoute.PROPERTY_TEMA) final String tema) {
		// hent metadata og berik modellen
		return Flowable.fromIterable(saker)
				.buffer(100)
				.parallel(10)
				.runOn(Schedulers.io())
				.map(saks -> {
					final Set<String> unikeAktoerids = saks.stream()
							.filter(s -> s.getBruker().isPerson())
							.map(s -> s.getBruker().getId())
							.collect(Collectors.toSet());
					final Map<String, Bruker> pdlHentIdenterBolks = pdlGraphQLConsumer.hentPersonBolk(unikeAktoerids, tema);
					return saks.stream()
							.map(s -> s.tilhoererBruker(pdlHentIdenterBolks.get(s.getBruker().getId())))
							.collect(Collectors.toList());
				})
				.flatMapIterable(items -> items)
				.sequential()
				.toList().subscribeOn(Schedulers.io()).blockingGet();
	}
}
