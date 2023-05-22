package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;

@Component
public class AvleveringArkivstrukturRoute extends RouteBuilder {
	public static final String ARKIVSTRUKTUR = "direct:arkivstruktur";

	private final AvleveringArkivstrukturService avleveringArkivstrukturService;

	public AvleveringArkivstrukturRoute(AvleveringArkivstrukturService avleveringArkivstrukturService) {
		this.avleveringArkivstrukturService = avleveringArkivstrukturService;
	}

	private JaxbDataFormat arkivstrukturJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		jaxbDataFormat.setSchemaLocation("http://www.arkivverket.no/standarder/noark5/arkivstruktur arkivstruktur.xsd");
		return jaxbDataFormat;
	}

	@Override
	public void configure() throws Exception {
		from(ARKIVSTRUKTUR)
				.routeId("arkivstruktur")
				.log(LoggingLevel.INFO, log, "Behandler arkivdeler for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.bean(avleveringArkivstrukturService)
				.marshal(arkivstrukturJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivstruktur.xml"))
				.to("file://{{avlevering.filomraade.work}}/?fileExist=Override")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("arkivstruktur.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");
	}
}
