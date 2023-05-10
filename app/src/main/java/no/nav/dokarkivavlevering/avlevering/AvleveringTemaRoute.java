package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.AvleveringArkivstrukturRoute;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.IdRange;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.dokument.DokumentRoute;
import no.nav.dokarkivavlevering.avlevering.endringlogg.AvleveringEndringsloggRoute;
import no.nav.dokarkivavlevering.avlevering.loependejournal.AvleveringLoependeJournalRoute;
import no.nav.dokarkivavlevering.avlevering.offentligjournal.AvleveringOffentligJournalRoute;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.dokarkivavlevering.avlevering.AvleveringRoute.PROPERTY_TEMA;
import static org.apache.camel.LoggingLevel.INFO;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class AvleveringTemaRoute extends RouteBuilder {

	public static final String BEHANDLE_TEMA = "direct:behandle_tema";
	public static final String BEHANDLE_TEMA_PAGE_MED_DOKUMENTER = "direct:behandle_tema_page_med_dokumenter";
	public static final String BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER = "direct:behandle_tema_page_uten_dokumenter";
	public static final String DETERMINE_AVLEVER_DOKUMENTER = "direct:determine_avlever_dokumenter";
	public static final String HEADER_AVLEVERING_TEMA_SIZE = "AvleveringTemaSize";
	public static final String HEADER_LAST_SAK_ID = "AvleveringLastSakId";
	public static final String HEADER_TEMA_SKIP = "AvleveringTemaSkip";
	public static final String PROPERTY_TEMA_IDRANGE = "AvleveringIdRange";
	public static final String PROPERTY_AVLEVER_MED_DOKUMENTER ="AvleverMedDokumenter";
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

		//@formatter:off
		from(BEHANDLE_TEMA)
				.routeId("behandle_tema")
				.removeHeaders("*")
				.removeProperty(PROPERTY_TEMA_IDRANGE)
				.log(INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}")
				.process(exchange -> {
					final Tema tema = exchange.getIn().getBody(Tema.class);
					final IdRange idRange = avleveringRepository.findJournalpostIdRange(tema);
					// Vi må passe på få med høyeste verdi siden vi sjekker alle sakId før max.
					exchange.getIn().setHeader(HEADER_LAST_SAK_ID, idRange.getSakIdMax() + 1);
					exchange.setProperty(PROPERTY_TEMA_IDRANGE, idRange);
					exchange.setProperty(PROPERTY_AVLEVER_MED_DOKUMENTER, tema.isAvleverDokumenter());
					log.info("Tema={} har idRange={}", tema, idRange);
				})
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, constant(avleveringProperties.getBatchsize())) // init paginering
				.loopDoWhile(exchange -> {
						final Long avleveringTemaSize = exchange.getIn().getHeader(HEADER_AVLEVERING_TEMA_SIZE, Long.class);
						return avleveringTemaSize >= avleveringProperties.getBatchsize();
					})
					.log(INFO, log,
							"Henter de neste ${header.AvleveringTemaSize} sakIds for tema=${exchangeProperty.AvleveringTema} før sakId=${header.AvleveringLastSakId}, " +
									"loop=${header.CamelLoopIndex}")
					.bean(avleveringRepository, "findSakIdsPagination")
					.log(INFO, log,"fikk hentet saker")
					.choice()
						.when(simple("${body.size} == 0 && ${header.CamelLoopIndex} == 0"))
							.log(INFO, log, "Ingen sakIds funnet for tema=${exchangeProperty.AvleveringTema}")
							.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
							.setHeader(HEADER_TEMA_SKIP, constant(true))
							.setBody(exchangeProperty(PROPERTY_TEMA))
						.otherwise()
							.to(DETERMINE_AVLEVER_DOKUMENTER)
						.end()// end choice
				.end() // end loop
				.choice()
					.when(header(HEADER_TEMA_SKIP).isEqualTo(constant(true)))
						.log(INFO, log, "Ingenting å avlevere for tema=${exchangeProperty.AvleveringTema}")
					// .otherwise()
						// .to(AvleveringArkivstrukturRoute.GENERER_KLASSE)
					.end()
				.log(INFO, log, "Ferdig behandlet tema=${exchangeProperty.AvleveringTema}");

		from(DETERMINE_AVLEVER_DOKUMENTER)
				.choice().when(exchangeProperty(PROPERTY_AVLEVER_MED_DOKUMENTER))
					.to(BEHANDLE_TEMA_PAGE_MED_DOKUMENTER)
				.otherwise()
					.to(BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER)
				.end();// end choice

		from(BEHANDLE_TEMA_PAGE_MED_DOKUMENTER)
				.routeId("behandle_tema_page_med_dokumenter")
				.setHeader(HEADER_LAST_SAK_ID, simple("${body[last]}"))
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
				.log(INFO, log,
						"behandle_tema_page_med_dokumenter behandler ${header.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}, " +
								"lastSakId=${header.AvleveringLastSakId}, loop=${header.CamelLoopIndex}")
				.bean(avleveringRepository, "findSakerMedDokumenter")
				.bean(avleveringSakBerikerService, "berikSakerMedDokumenter")
				.multicast((oldExchange, newExchange) -> {
					if (oldExchange == null) {
						// Setter denne på body da den er input til loopen. Data på body etter aggregeringen blir da slettet fra minne.
						newExchange.getIn().setBody(newExchange.getProperty(PROPERTY_TEMA));
						return newExchange;
					}
					return oldExchange;
				})
				.parallelProcessing()
				.to(
						AvleveringArkivstrukturRoute.ARKIVSTRUKTUR,
						AvleveringEndringsloggRoute.ENDRINGSLOGG,
						AvleveringLoependeJournalRoute.LOEPENDEJOURNAL,
						AvleveringOffentligJournalRoute.OFFENTLIGJOURNAL,
						DokumentRoute.SEND_DOKUMENT
				)
				.end(); // end multicast

		from(BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER)
				.routeId("behandle_tema_page_uten_dokumenter")
				.setHeader(HEADER_LAST_SAK_ID, simple("${body[last]}"))
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
				.log(INFO, log,
						"behandle_tema_page_uten_dokumenter behandler ${header.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}, " +
								"lastSakId=${header.AvleveringLastSakId}, loop=${header.CamelLoopIndex}")
				.bean(avleveringRepository, "findSakerUtenDokumenter")
				.bean(avleveringSakBerikerService, "berikSakerUtenDokumenter")
				.multicast((oldExchange, newExchange) -> {
					if (oldExchange == null) {
						// Setter denne på body da den er input til loopen. Data på body etter aggregeringen blir da slettet fra minne.
						newExchange.getIn().setBody(newExchange.getProperty(PROPERTY_TEMA));
						return newExchange;
					}
					return oldExchange;
				})
				.parallelProcessing()
				.to(
						AvleveringArkivstrukturRoute.ARKIVSTRUKTUR,
						AvleveringEndringsloggRoute.ENDRINGSLOGG,
						AvleveringLoependeJournalRoute.LOEPENDEJOURNAL,
						AvleveringOffentligJournalRoute.OFFENTLIGJOURNAL
				)
				.end(); // end multicast
		//@formatter:on

	}
}
