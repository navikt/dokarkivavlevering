package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class AvleveringArkivstrukturRoute extends RouteBuilder {

	public static final String HEADER_XSL_PARAM_KLASSE_XML = "klasse_xml";
	public static final String HEADER_XSL_PARAM_SAKSMAPPE_XML = "saksmappe_xml";
	public static final String GENERER_ARKIVSTRUKTUR = "direct:generer_arkivstruktur";
	public static final String OPPRETT_ARKIVSTRUKTUR_PRE = "direct:opprett_arkivstruktur_pre";
	public static final String FLETT_KLASSE_ARKIVSTRUKTUR = "direct:flett_klasse_arkivstruktur";
	public static final String ARKIVSTRUKTUR = "direct:arkivstruktur";
	public static final String GENERER_KLASSE = "direct:generer_klasse";
	public static final String OPPRETT_KLASSE_PRE = "direct:opprett_klasse_pre";
	public static final String FLETT_SAKSMAPPE_KLASSE = "direct:flett_saksmappe_klasse";

	private final AvleveringArkivstrukturService avleveringArkivstrukturService;
	private final ArkivMapper arkivMapper;

	public AvleveringArkivstrukturRoute(AvleveringArkivstrukturService avleveringArkivstrukturService, ArkivMapper arkivMapper, KlasseMapper klasseMapper) {
		this.avleveringArkivstrukturService = avleveringArkivstrukturService;
		this.arkivMapper = arkivMapper;
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
		from(GENERER_ARKIVSTRUKTUR)
				.routeId("generer_arkivstruktur")
				.log(LoggingLevel.INFO, log, "Starter generering av arkivstruktur.xml.")
				.to(OPPRETT_ARKIVSTRUKTUR_PRE)
				.to(FLETT_KLASSE_ARKIVSTRUKTUR)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");

		from(OPPRETT_ARKIVSTRUKTUR_PRE)
				.routeId("opprett_arkivstruktur_pre")
				.bean(arkivMapper)
				.marshal(arkivstrukturJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivstruktur_pre.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte arkivstruktur til ${header.CamelFileNameProduced}");

		from(FLETT_KLASSE_ARKIVSTRUKTUR)
				.routeId("flett_klasse_arkivstruktur")
				.process(exchange -> {
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_KLASSE_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}?select=klasse_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/arkivstruktur.xml"))
				.to("xslt:classpath:arkivstruktur/embed_klasse_into_arkivstruktur.xsl?output=file")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("arkivstruktur.xml"))
				.to(AvleveringSFTPRoute.SFTP);

		from(ARKIVSTRUKTUR)
				.routeId("arkivstruktur")
				.log(LoggingLevel.INFO, log, "Behandler saksmapper for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.bean(avleveringArkivstrukturService)
				.process(exchange -> {
					// Dummy struktur slik at xml blir well-formed. Dvs at <mappe> ligger under <klasse>
					Klasse klasse = new Klasse();
					klasse.getMappes().addAll(exchange.getIn().getBody(List.class));
					exchange.getIn().setBody(klasse);
				})
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}klasse"))
				.marshal(klasseArkivstrukturJaxb())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/saksmappe_${exchangeProperty.AvleveringTema}_${header.CamelLoopIndex}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Append")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig saksmapper for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");

		from(GENERER_KLASSE)
				.routeId("generer_klasse")
				.log(LoggingLevel.INFO, log, "Starter generering av klasse_${exchangeProperty.AvleveringTema}.xml.")
				.to(OPPRETT_KLASSE_PRE)
				.to(FLETT_SAKSMAPPE_KLASSE)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");

		/*
		from(OPPRETT_KLASSE_PRE)
				.routeId("opprett_klasse_pre")
				.bean(klasseMapper)
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}klasse"))
				.marshal(klasseArkivstrukturJaxb())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/pre_klasse_${exchangeProperty.AvleveringTema}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte pre_klasse til ${header.CamelFileNameProduced}");
		 */

		from(FLETT_SAKSMAPPE_KLASSE)
				.routeId("flett_saksmappe_klasse")
				.process(exchange -> {
					// Fra opprett_klasse_pre
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_SAKSMAPPE_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/?select=saksmappe_${exchangeProperty.AvleveringTema}_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/klasse_${exchangeProperty.AvleveringTema}.xml"))
				.to("xslt:classpath:arkivstruktur/embed_saksmappe_into_klasse.xsl?output=file")
				.log(LoggingLevel.INFO, log, "Genererte klasse til ${header.CamelXsltFileName}");
	}
}
