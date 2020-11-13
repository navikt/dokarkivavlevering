package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class AvleveringSakService {
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public AvleveringSakService(PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	@Handler
	public void avlevering(final List<Sak> saker) {
		// start forretningslogikk her
		// map til xmlstruktur
		// hent metadata
		// returner liste av xml objekte for marshal
	}
}
