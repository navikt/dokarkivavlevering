package no.nav.dokarkivavlevering.avlevering;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.AvleveringArkivstrukturRoute;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.dokument.DokumentRoute;
import no.nav.dokarkivavlevering.avlevering.endringlogg.AvleveringEndringsloggRoute;
import no.nav.dokarkivavlevering.avlevering.loependejournal.AvleveringLoependeJournalRoute;
import no.nav.dokarkivavlevering.avlevering.offentligjournal.AvleveringOffentligJournalRoute;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokarkivavlevering.avlevering.AvleveringRoute.PROPERTY_TEMA;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class AvleveringTemaRoute extends RouteBuilder {

	public static final String BEHANDLE_TEMA = "direct:behandle_tema";
	public static final String BEHANDLE_TEMA_PAGE_MED_DOKUMENTER = "direct:behandle_tema_page_med_dokumenter";
	public static final String BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER = "direct:behandle_tema_page_uten_dokumenter";
	public static final String DETERMINE_AVLEVER_DOKUMENTER = "direct:determine_avlever_dokumenter";
	public static final String HEADER_TEMA_SKIP = "AvleveringTemaSkip";
	public static final String PROPERTY_AVLEVER_MED_DOKUMENTER = "AvleverMedDokumenter";
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
				.log(INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}")
				.process(exchange -> {
					final Tema tema = exchange.getIn().getBody(Tema.class);
					exchange.setProperty(PROPERTY_AVLEVER_MED_DOKUMENTER, tema.isAvleverDokumenter());
					List<Long> sakIds = avleveringRepository.findSakIds(tema);
					exchange.getIn().setBody(Lists.partition(sakIds, avleveringProperties.getBatchsize()));
					log.info("Tema={} har {} saker som skal avleveres", tema, sakIds.size());
				})
				.split(body())
					.choice()
						.when(simple("${body.size} == 0 && ${header.camelSplitIndex} == 0"))
							.log(INFO, log, "Ingen sakIds funnet for tema=${exchangeProperty.AvleveringTema}")
							.setHeader(HEADER_TEMA_SKIP, constant(true))
							.setBody(exchangeProperty(PROPERTY_TEMA))
						.otherwise()
							.to(DETERMINE_AVLEVER_DOKUMENTER)
					.end()// end choice
				.end() // end split
				.choice()
					.when(header(HEADER_TEMA_SKIP).isEqualTo(constant(true)))
						.log(INFO, log, "Ingenting å avlevere for tema=${exchangeProperty.AvleveringTema}")
				.end()
				//Tema er input i neste part av routen
				.process(exchange -> exchange.getIn().setBody(exchange.getProperty(PROPERTY_TEMA)))
				.log(INFO, log, "Ferdig behandlet tema=${exchangeProperty.AvleveringTema}");

		from(DETERMINE_AVLEVER_DOKUMENTER)
				.choice()
					.when(exchangeProperty(PROPERTY_AVLEVER_MED_DOKUMENTER))
						.to(BEHANDLE_TEMA_PAGE_MED_DOKUMENTER)
					.otherwise()
						.to(BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER)
				.end();// end choice

		from(BEHANDLE_TEMA_PAGE_MED_DOKUMENTER)
				.routeId("behandle_tema_page_med_dokumenter")
				.log(INFO, log,
						"behandle_tema_page_med_dokumenter behandler neste batch med sakId'er for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}")
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
						AvleveringArkivstrukturRoute.SAKSMAPPE,
						AvleveringEndringsloggRoute.ENDRINGSLOGG,
						AvleveringLoependeJournalRoute.LOEPENDEJOURNAL,
						AvleveringOffentligJournalRoute.OFFENTLIGJOURNAL,
						DokumentRoute.SEND_DOKUMENT
				)
				.end(); // end multicast

		from(BEHANDLE_TEMA_PAGE_UTEN_DOKUMENTER)
				.routeId("behandle_tema_page_uten_dokumenter")
				.log(INFO, log,
						"behandle_tema_page_uten_dokumenter behandler neste batch med sakId'er for tema=${exchangeProperty.AvleveringTema}, " +
								"lastSakId=${header.AvleveringLastSakId}, loop=${header.CamelSplitIndex}")
				.log(INFO, log, "findSakerUtenDokumenter start")
				.bean(avleveringRepository, "findSakerUtenDokumenter")
				.log(INFO, log, "findSakerUtenDokumenter end")
				.log(INFO, log, "berikSakerUtenDokumenter start")
				.bean(avleveringSakBerikerService, "berikSakerUtenDokumenter")
				.log(INFO, log, "berikSakerUtenDokumenter end")
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
						AvleveringArkivstrukturRoute.SAKSMAPPE,
						AvleveringEndringsloggRoute.ENDRINGSLOGG,
						AvleveringLoependeJournalRoute.LOEPENDEJOURNAL,
						AvleveringOffentligJournalRoute.OFFENTLIGJOURNAL
				)
				.end(); // end multicast
		//@formatter:on

	}
}
