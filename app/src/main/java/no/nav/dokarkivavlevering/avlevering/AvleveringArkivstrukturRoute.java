package no.nav.dokarkivavlevering.avlevering;

import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.ArkivMapper;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class AvleveringArkivstrukturRoute extends RouteBuilder {

	public static final String HEADER_XSL_PARAM_KLASSE_XML = "klasse_xml";

	private final ArkivMapper arkivMapper;

	public AvleveringArkivstrukturRoute(ArkivMapper arkivMapper) {
		this.arkivMapper = arkivMapper;
	}

	private JaxbDataFormat arkivstrukturJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		return jaxbDataFormat;
	}

	@Override
	public void configure() throws Exception {
		from("direct:generer_arkivstruktur")
				.routeId("generer_arkivstruktur")
				.log(LoggingLevel.INFO, log, "Starter generering av arkivstruktur.xml.")
				.to("direct:opprett_arkivstruktur_pre")
				.to("direct:flett_klasse_arkivstruktur")
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");

		from("direct:opprett_arkivstruktur_pre")
				.routeId("opprett_arkivstruktur_pre")
				.bean(arkivMapper)
				.marshal(arkivstrukturJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivstruktur_pre.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte arkivstruktur til ${header.CamelFileNameProduced}");

		from("direct:flett_klasse_arkivstruktur")
				.routeId("flett_klasse_arkivstruktur")
				.process(exchange -> {
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_KLASSE_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}?select=klasse_*.xml"))
				.to("xslt:classpath:arkivstruktur/embed_klasse_into_arkivstruktur.xsl?output=bytes")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("arkivstruktur.xml"))
				.to(AvleveringSFTPRoute.SFTP);
	}
}
