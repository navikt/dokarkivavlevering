package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Component
@Profile("genererAvlevering")
public class AvleveringArkivstrukturRoute extends RouteBuilder {
	public static final String SAKSMAPPE = "direct:saksmappe";
	public static final String ARKIV = "direct:arkiv";
	public static final String ARKIVSTRUKTUR = "direct:arkivstruktur";
	public static final String HEADER_XSL_PARAM_SAKSMAPPE_XML = "saksmappe_xml";

	private final AvleveringArkivstrukturService avleveringArkivstrukturService;
	private final AvleveringArkivstrukturKlasseService avleveringArkivstrukturKlasseService;
	private final AvleveringRepository avleveringRepository;

	public AvleveringArkivstrukturRoute(AvleveringArkivstrukturService avleveringArkivstrukturService,
										AvleveringArkivstrukturKlasseService avleveringArkivstrukturKlasseService,
										AvleveringRepository avleveringRepository) {
		this.avleveringArkivstrukturService = avleveringArkivstrukturService;
		this.avleveringArkivstrukturKlasseService = avleveringArkivstrukturKlasseService;
		this.avleveringRepository = avleveringRepository;
	}

	private JaxbDataFormat klasseArkivstrukturJaxb() {
		JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(ObjectFactory.class.getPackage().getName());
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setFragment(true);
		jaxbDataFormat.setPartClass(Klasse.class);
		return jaxbDataFormat;
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
		from(SAKSMAPPE)
				.routeId("saksmappe_innhold")
				.log(LoggingLevel.INFO, log, "Behandler Saksmapper for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}")
				.bean(avleveringArkivstrukturKlasseService)
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}klasse"))
				.marshal(klasseArkivstrukturJaxb())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/saksmapper_${header.CamelSplitIndex}.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}/?fileExist=Append");

		from(ARKIV)
				.routeId("arkiv_arkivdel_klasse_innhold")
				.bean(avleveringRepository, "getFagomradeForTema")
				.bean(avleveringArkivstrukturService)
				.marshal(arkivstrukturJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivstruktur_tmp.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}/?fileExist=Override")
				.to(ARKIVSTRUKTUR)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere overordnet arkivstruktur for tema ${exchangeProperty.AvleveringTema}");

		from(ARKIVSTRUKTUR)
				.routeId("arkivstruktur")
				.process(exchange -> {
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_SAKSMAPPE_XML, simple("file:///{{dokarkivavlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/?select=saksmapper_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{dokarkivavlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/klasse_${exchangeProperty.AvleveringTema}.xml"))
				.to("xslt-saxon:classpath:arkivstruktur/embed_saksmappe_into_klasse.xsl?output=file")
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivstruktur.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}/?fileExist=Override")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("arkivstruktur.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");
	}
}
