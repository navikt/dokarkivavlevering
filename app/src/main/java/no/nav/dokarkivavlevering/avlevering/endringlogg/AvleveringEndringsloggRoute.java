package no.nav.dokarkivavlevering.avlevering.endringlogg;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.arkivverket.standarder.noark5.endringslogg.Endringslogg;
import no.arkivverket.standarder.noark5.endringslogg.ObjectFactory;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@Component
public class AvleveringEndringsloggRoute extends RouteBuilder {

	public static final String HEADER_XSL_PARAM_ENDRING_XML = "endring_xml";
	public static final String GENERER_ENDRINGSLOGG = "direct:generer_endringslogg";
	public static final String OPPRETT_ENDRINGSLOGG = "direct:opprett_endringslogg_pre";
	public static final String FLETT_ENDRINGSLOGG = "direct:flett_endringslogg";
	public static final String ENDRINGSLOGG = "direct:endringslogg";

	private EndringsloggService endringsloggService;

	public AvleveringEndringsloggRoute(EndringsloggService endringsloggService) {
		this.endringsloggService = endringsloggService;
	}

	private JaxbDataFormat endringsloggJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		jaxbDataFormat.setSchemaLocation("http://www.arkivverket.no/standarder/noark5/endringslogg endringslogg.xsd");
		return jaxbDataFormat;
	}

	@Override
	public void configure() throws Exception {
		from(GENERER_ENDRINGSLOGG)
				.routeId("generer_endringslogg")
				.log(LoggingLevel.INFO, log, "Starter generering av endringslogg.xml.")
				.to(OPPRETT_ENDRINGSLOGG)
				.to(FLETT_ENDRINGSLOGG)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere endringslogg.xml.");

		from(OPPRETT_ENDRINGSLOGG)
				.routeId("opprett_endringslogg_pre")
				.process(exchange -> {
					exchange.getIn().setBody(new Endringslogg());
				})
				.marshal(endringsloggJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/endringslogg_pre.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte endringslogg til ${header.CamelFileNameProduced}");

		from(FLETT_ENDRINGSLOGG)
				.routeId("flett_endringslogg")
				.process(exchange -> {
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_ENDRING_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}?select=endring_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/endringslogg.xml"))
				.to("xslt-saxon:classpath:endringslogg/embed_arkivendring_into_endringslogg.xsl?output=file")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("endringslogg.xml"))
				.to(AvleveringSFTPRoute.SFTP);

		from(ENDRINGSLOGG)
				.routeId("endringslogg")
				.log(LoggingLevel.INFO, log, "Behandler endringslogg.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}")
				.bean(endringsloggService)
				.process(exchange -> {
					Endringslogg endringslogg = new Endringslogg();
					endringslogg.getEndrings().addAll(exchange.getIn().getBody(List.class));
					exchange.getIn().setBody(endringslogg);
				})
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/endring_${exchangeProperty.AvleveringTema}_${header.CamelSplitIndex}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig ${header.CamelFilenameProduced} for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}");
	}
}
