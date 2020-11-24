package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Mappe;
import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class AvleveringTemaRoute extends RouteBuilder {

	public static final String HEADER_AVLEVERING_TEMA_SIZE = "AvleveringTemaSize";
	public static final String HEADER_LAST_SAK_ID = "AvleveringLastSakId";
	private final AvleveringProperties avleveringProperties;
	private final AvleveringRepository avleveringRepository;
	private final AvleveringSakService avleveringSakService;
	private final AvleveringSakBerikerService avleveringSakBerikerService;

	@Autowired
	public AvleveringTemaRoute(AvleveringProperties avleveringProperties, AvleveringRepository avleveringRepository,
							   AvleveringSakService avleveringSakService, AvleveringSakBerikerService avleveringSakBerikerService) {
		this.avleveringProperties = avleveringProperties;
		this.avleveringRepository = avleveringRepository;
		this.avleveringSakService = avleveringSakService;
		this.avleveringSakBerikerService = avleveringSakBerikerService;
	}

	private JaxbDataFormat saksmappeArkivstrukturJaxb() {
		JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(ObjectFactory.class.getPackage().getName());
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setFragment(true);
		jaxbDataFormat.setPartClass(Saksmappe.class);
		return jaxbDataFormat;
	}

	private JaxbDataFormat klasseArkivstrukturJaxb() {
		JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(ObjectFactory.class.getPackage().getName());
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setFragment(true);
		jaxbDataFormat.setPartClass(Klasse.class);
		return jaxbDataFormat;
	}

	@Override
	public void configure() throws Exception {
		errorHandler(noErrorHandler());

		from("direct:behandle_tema")
				.routeId("behandle_tema")
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}")
				.setHeader(HEADER_LAST_SAK_ID, constant(Long.MAX_VALUE)) // init paginering
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, constant(avleveringProperties.getPeriode().getBatchsize())) // init paginering
				.loopDoWhile(new Predicate() {
					@Override
					public boolean matches(Exchange exchange) {
						final Long avleveringTemaSize = exchange.getIn().getHeader(HEADER_AVLEVERING_TEMA_SIZE, Long.class);
						return avleveringTemaSize >= avleveringProperties.getPeriode().getBatchsize();
					}
				})
				.log(LoggingLevel.INFO, log,
						"Henter de neste ${header.AvleveringTemaSize} sakIds for tema=${exchangeProperty.AvleveringTema} før sakId=${header.AvleveringLastSakId}, " +
								"loop=${header.CamelLoopIndex}")
				.bean(avleveringRepository, "findSakIdsPagination")
				.setHeader(HEADER_LAST_SAK_ID, simple("${body[last]}"))
				.setHeader(HEADER_AVLEVERING_TEMA_SIZE, simple("${body.size}"))
				.log(LoggingLevel.INFO, log,
						"Behandler ${header.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}, " +
								"loop=${header.CamelLoopIndex}, lastSakId=${header.AvleveringLastSakId}")
				.bean(avleveringRepository, "findSaker")
				.bean(avleveringSakBerikerService)
				.multicast((oldExchange, newExchange) -> {
					if(oldExchange == null) {
						// Setter denne på body da den er input til loopen. Data på body etter aggregeringen blir da slettet fra minne.
						newExchange.getIn().setBody(newExchange.getProperty(AvleveringRoute.PROPERTY_TEMA));
						return newExchange;
					}
					return oldExchange;
				})
				.parallelProcessing()
				.to("direct:arkivstruktur", "direct:endringslogg", "direct:loependeJournal", "direct:offentligJournal")
				.end() // end multicast
				.end() // end loop
				.log(LoggingLevel.INFO, log, "Ferdig behandlet tema=${exchangeProperty.AvleveringTema}");

		// Denne skilles ut i en egen Route klasse. Impementasjon må være trådsikker pga dette kjører i en egen tråd.
		from("direct:arkivstruktur")
				.routeId("arkivstruktur")
				.log(LoggingLevel.INFO, log, "Behandler arkivstruktur.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.bean(avleveringSakService)
				.split(body()).streaming()
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}saksmappe"))
				.marshal(saksmappeArkivstrukturJaxb())
				.end()
				.process(exchange -> {
					//FIXME her må man ha generert hele <klasse>
					Klasse klasse = new Klasse();
					klasse.getMappes().addAll((List<Mappe>)exchange.getIn().getExchange().getIn().getBody());
					klasse.setTittel(exchange.getProperty(AvleveringRoute.PROPERTY_TEMA, String.class));
					exchange.getIn().setBody(klasse);
				})
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}klasse"))
				.marshal(klasseArkivstrukturJaxb())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/klasse_${exchangeProperty.AvleveringTema}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig arkivstruktur.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");

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
