package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Mappe;
import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
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
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering behandler tema=${exchangeProperty.AvleveringTema}.")
				.bean(avleveringRepository, "findSakIdForTema")
				.setProperty(PROPERTY_TEMA_SIZE, simple("${body.size}"))
				.log(LoggingLevel.INFO, log, "Fant ${exchangeProperty.AvleveringTemaSize} sakId for tema=${exchangeProperty.AvleveringTema}.")
				.bean(avleveringRepository, "findSaker")
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
				.log(LoggingLevel.INFO, log, "Behandlet tema=${exchangeProperty.AvleveringTema}.");
	}

}
