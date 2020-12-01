package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
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

@Component
public class AvleveringLoependeJournalRoute extends RouteBuilder {

	public static final String HEADER_XSL_PARAM_LOEPENDEJOURNAL_XML = "loependejournal_xml";
	public static final String GENERER_LOEPENDEJOURNAL = "direct:generer_loependejournal";
	public static final String OPPRETT_LOEPENDEJOURNAL_PRE = "direct:opprett_loependejournal_pre";
	public static final String FLETT_LOEPENDEJOURNAL = "direct:flett_loependeJournal";
	public static final String LOEPENDEJOURNAL = "direct:loependeJournal";

	private JaxbDataFormat marshalJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		jaxbDataFormat.setSchemaLocation("http://www.arkivverket.no/standarder/noark5/loependeJournal loependeJournal.xsd");
		return jaxbDataFormat;
	}

	private JournalregistreringService journalregistreringService;
	private LoependejournalMapper loependejournalMapper;

	public AvleveringLoependeJournalRoute(JournalregistreringService journalregistreringService, LoependejournalMapper loependejournalMapper) {
		this.journalregistreringService = journalregistreringService;
		this.loependejournalMapper = loependejournalMapper;
	}

	@Override
	public void configure() throws Exception {

		from(GENERER_LOEPENDEJOURNAL)
				.routeId("generer_loependeJournal")
				.log(LoggingLevel.INFO, log, "Starter generering av loependejournal.xml.")
				.to(OPPRETT_LOEPENDEJOURNAL_PRE)
				.to(FLETT_LOEPENDEJOURNAL)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivstruktur.xml.");

		from(OPPRETT_LOEPENDEJOURNAL_PRE)
				.routeId("opprett_loependeJournal_pre")
				.log(LoggingLevel.INFO, log, "loependejournal_pre")
				.bean(loependejournalMapper)
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}loependejournal"))
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/pre_loependeJournal.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte pre_loependeJournal til ${header.CamelFileNameProduced}");

		from(FLETT_LOEPENDEJOURNAL)
				.routeId("flett_loependeJournal")
				.log(LoggingLevel.INFO, log, "flett_loependejournal")
				.process(exchange -> {
					// Fra opprett_loependeJournal_pre
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_LOEPENDEJOURNAL_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/?select=journalregistrering_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/loependejournal.xml"))
				.to("xslt:classpath:loependejournal/embed_registrering_into_loependejournal.xsl?output=file")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("loependeJournal.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Genererte loependeJournal til ${header.CamelXsltFileName}");


		from(LOEPENDEJOURNAL)
				.routeId("loependeJournal")
				.log(LoggingLevel.INFO, log, "RouteID: loependejournal")
				.log(LoggingLevel.INFO, log, "Behandler loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.bean(journalregistreringService)
				.process(exchange -> {
					LoependeJournal journal = new LoependeJournal();
					journal.getJournalregistrerings().addAll((List<Journalregistrering>) exchange.getIn().getBody());
					exchange.getIn().setBody(journal);
				})
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}loependejournal"))
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/journalregistrering_${exchangeProperty.AvleveringTema}_${header.CamelLoopIndex}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Append")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");
	}
}
