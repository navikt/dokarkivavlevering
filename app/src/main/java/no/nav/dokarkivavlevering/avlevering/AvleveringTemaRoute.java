package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
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

	public static final String PROPERTY_TEMA = "AvleveringTema";
	public static final String PROPERTY_TEMA_SIZE = "AvleveringTemaSize";
	private final AvleveringProperties avleveringProperties;
	private final AvleveringRepository avleveringRepository;
	private final AvleveringSakService avleveringSakService;

	@Autowired
	public AvleveringTemaRoute(AvleveringProperties avleveringProperties, AvleveringRepository avleveringRepository,
							   AvleveringSakService avleveringSakService) {
		this.avleveringProperties = avleveringProperties;
		this.avleveringRepository = avleveringRepository;
		this.avleveringSakService = avleveringSakService;
	}

	@Override
	public void configure() throws Exception {
		errorHandler(noErrorHandler());

		from("direct:behandle_tema")
				.routeId("behandle_tema")
				.setProperty(PROPERTY_TEMA, body())
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}.")
				.bean(avleveringRepository, "findSakIdForTema")
				.setProperty(PROPERTY_TEMA_SIZE, simple("${body.size}"))
				.log(LoggingLevel.INFO, log, "Fant ${exchangeProperty.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}.")
				.bean(avleveringRepository, "findSaker")
				.bean(avleveringSakService)
				// skriv bolk til arbeidsmappe
				.end()
				.log(LoggingLevel.INFO, log, "Behandlet tema=${exchangeProperty.AvleveringTema}.");
	}
}
