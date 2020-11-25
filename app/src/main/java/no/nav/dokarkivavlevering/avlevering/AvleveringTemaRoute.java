package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.AvleveringArkivstrukturRoute;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokarkivavlevering.avlevering.AvleveringRoute.PROPERTY_TEMA;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class AvleveringTemaRoute extends RouteBuilder {

	public static final String BEHANDLE_TEMA = "direct:behandle_tema";
	public static final String BEHANDLE_TEMA_PAGE = "direct:behandle_tema_page";
	public static final String HEADER_AVLEVERING_TEMA_SIZE = "AvleveringTemaSize";
	public static final String HEADER_LAST_SAK_ID = "AvleveringLastSakId";
	public static final String HEADER_TEMA_SKIP = "AvleveringTemaSkip";
	private final AvleveringProperties avleveringProperties;
	private final AvleveringRepository avleveringRepository;
	private final AvleveringSakBerikerService avleveringSakBerikerService;

	@Autowired
	public AvleveringTemaRoute(AvleveringProperties avleveringProperties, AvleveringRepository avleveringRepository,
							   AvleveringSakBerikerService avleveringSakBerikerService) {
		this.avleveringProperties = avleveringProperties;
		this.avleveringRepository = avleveringRepository;
		this.avleveringSakBerikerService = avleveringSakBerikerService;
	}

	@Override
	public void configure() throws Exception {
		errorHandler(noErrorHandler());

		from(BEHANDLE_TEMA)
				.routeId("behandle_tema")
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}")
				.setHeader(HEADER_LAST_SAK_ID, constant(Long.MAX_VALUE)) // init paginering
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, constant(avleveringProperties.getBatchsize())) // init paginering
				.loopDoWhile(exchange -> {
					final Long avleveringTemaSize = exchange.getIn().getHeader(HEADER_AVLEVERING_TEMA_SIZE, Long.class);
					return avleveringTemaSize >= avleveringProperties.getBatchsize();
				})
				.log(LoggingLevel.INFO, log,
						"Henter de neste ${header.AvleveringTemaSize} sakIds for tema=${exchangeProperty.AvleveringTema} før sakId=${header.AvleveringLastSakId}, " +
								"loop=${header.CamelLoopIndex}")
				.bean(avleveringRepository, "findSakIdsPagination")
				.choice().when(simple("${body.size} == 0 && ${header.CamelLoopIndex} == 0"))
				.log(LoggingLevel.INFO, log, "Ingen sakIds funnet for tema=${exchangeProperty.AvleveringTema}")
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
				.setHeader(HEADER_TEMA_SKIP, constant(true))
				.setBody(exchangeProperty(PROPERTY_TEMA))
				.otherwise()
				.to(BEHANDLE_TEMA_PAGE)
				.end()// end choice
				.end() // end loop
				.choice().when(header(HEADER_TEMA_SKIP).isEqualTo(constant(true)))
				.log(LoggingLevel.INFO, log, "Ingenting å avlevere for tema=${exchangeProperty.AvleveringTema}")
				.otherwise()
				.to(AvleveringArkivstrukturRoute.GENERER_KLASSE)
				.end()
				.log(LoggingLevel.INFO, log, "Ferdig behandlet tema=${exchangeProperty.AvleveringTema}");

		from(BEHANDLE_TEMA_PAGE)
				.routeId("behandle_tema_page")
				.setHeader(HEADER_LAST_SAK_ID, simple("${body[last]}"))
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
				.log(LoggingLevel.INFO, log,
						"Behandler ${header.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}, " +
								"lastSakId=${header.AvleveringLastSakId}, loop=${header.CamelLoopIndex}")
				.bean(avleveringRepository, "findSaker")
				.bean(avleveringSakBerikerService)
				.multicast((oldExchange, newExchange) -> {
					if (oldExchange == null) {
						// Setter denne på body da den er input til loopen. Data på body etter aggregeringen blir da slettet fra minne.
						newExchange.getIn().setBody(newExchange.getProperty(PROPERTY_TEMA));
						return newExchange;
					}
					return oldExchange;
				})
				.parallelProcessing()
				.to(AvleveringArkivstrukturRoute.ARKIVSTRUKTUR, "direct:endringslogg", "direct:loependeJournal", "direct:offentligJournal")
				.end(); // end multicast

		// Denne skilles ut i en egen Route klasse. Impementasjon må være trådsikker pga dette kjører i en egen tråd.
		from("direct:endringslogg")
				.routeId("endringslogg")
				.log(LoggingLevel.INFO, log, "Behandler endringslogg.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.process(exchange -> {
					// Her skal body inneholde en List<Sak> som er ferdig beriket.
					final List<Sak> berikedeSaker = exchange.getIn().getBody(List.class);
					log.info("{} berikede saker for endringslogg.xml", berikedeSaker.size());
				})
				.log(LoggingLevel.INFO, log, "Behandlet ferdig endringslogg.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");

		// Denne skilles ut i en egen Route klasse. Impementasjon må være trådsikker pga dette kjører i en egen tråd.
		from("direct:loependeJournal")
				.routeId("loependeJournal")
				.log(LoggingLevel.INFO, log, "Behandler loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.process(exchange -> {
					// Her skal body inneholde en List<Sak> som er ferdig beriket.
					final List<Sak> berikedeSaker = exchange.getIn().getBody(List.class);
					log.info("{} berikede saker for loependeJournal.xml", berikedeSaker.size());
				})
				.log(LoggingLevel.INFO, log, "Behandlet ferdig loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");

		// Denne skilles ut i en egen Route klasse. Impementasjon må være trådsikker pga dette kjører i en egen tråd.
		from("direct:offentligJournal")
				.routeId("offentligJournal")
				.log(LoggingLevel.INFO, log, "Behandler offentligJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.process(exchange -> {
					// Her skal body inneholde en List<Sak> som er ferdig beriket.
					final List<Sak> berikedeSaker = exchange.getIn().getBody(List.class);
					log.info("{} berikede saker for offentligJournal.xml", berikedeSaker.size());
				})
				.log(LoggingLevel.INFO, log, "Behandlet ferdig offentligJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");
	}


}
