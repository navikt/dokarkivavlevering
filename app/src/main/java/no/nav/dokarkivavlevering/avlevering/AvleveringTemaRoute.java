package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class AvleveringTemaRoute extends RouteBuilder {

	private final AvleveringProperties avleveringProperties;

	@Autowired
	public AvleveringTemaRoute(AvleveringProperties avleveringProperties) {
		this.avleveringProperties = avleveringProperties;
	}

	@Override
	public void configure() throws Exception {
		from("direct:behandle_tema")
				.routeId("behandle_tema")
				// Forretningslogikk her
				.log(LoggingLevel.INFO, log, "Dokarkiv behandler tema.");

	}
}
