package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.SaksmappeMapper;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringSakService {
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final SaksmappeMapper saksmappeMapper;

	public AvleveringSakService(PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	@Handler
	public List<Saksmappe> avlevering(final List<Sak> saker) {
		// start forretningslogikk her
		// map til xmlstruktur
		// hent metadata
		// returner liste av xml objekter for marshal
		return saker.stream().map(saksmappeMapper::map)
				.collect(Collectors.toList());
	}
}
