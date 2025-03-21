package no.nav.dokarkivavlevering.avlevering.loependejournal;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
import no.arkivverket.standarder.noark5.loependejournal.ObjectFactory;
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

	private final LoependeJournalregistreringService loependeJournalregistreringService;
	private final LoependejournalMapper loependejournalMapper;

	public AvleveringLoependeJournalRoute(LoependeJournalregistreringService loependeJournalregistreringService, LoependejournalMapper loependejournalMapper) {
		this.loependeJournalregistreringService = loependeJournalregistreringService;
		this.loependejournalMapper = loependejournalMapper;
	}

	@Override
	public void configure() throws Exception {

		from(GENERER_LOEPENDEJOURNAL)
				.routeId("generer_loependeJournal")
				.log(LoggingLevel.INFO, log, "Starter generering av loependeJournal.xml.")
				.to(OPPRETT_LOEPENDEJOURNAL_PRE)
				.to(FLETT_LOEPENDEJOURNAL)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere loependeJournal.xml.");

		from(OPPRETT_LOEPENDEJOURNAL_PRE)
				.routeId("opprett_loependeJournal_pre")
				.log(LoggingLevel.INFO, log, "loependejournal_pre")
				.bean(loependejournalMapper)
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/pre_loependeJournal.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte pre_loependeJournal til ${header.CamelFileNameProduced}");

		from(FLETT_LOEPENDEJOURNAL)
				.routeId("flett_loependeJournal")
				.log(LoggingLevel.INFO, log, "flett_loependejournal")
				.process(exchange -> {
					// Fra opprett_loependeJournal_pre
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_LOEPENDEJOURNAL_XML, simple("file:///{{dokarkivavlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/?select=loependejournal_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{dokarkivavlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/loependeJournal.xml"))
				.to("xslt-saxon:classpath:loependejournal/embed_registrering_into_loependejournal.xsl?output=file")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("loependeJournal.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Genererte loependeJournal til ${header.CamelXsltFileName}");


		from(LOEPENDEJOURNAL)
				.routeId("loependeJournal")
				.log(LoggingLevel.INFO, log, "Behandler loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}")
				.bean(loependeJournalregistreringService)
				.process(exchange -> {
					LoependeJournal journal = new LoependeJournal();
					journal.getJournalregistrerings().addAll((List<Journalregistrering>) exchange.getIn().getBody());
					exchange.getIn().setBody(journal);
				})
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/loependejournal_${exchangeProperty.AvleveringTema}_${header.CamelSplitIndex}.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}?fileExist=Append")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig ${header.CamelFilenameProduced} for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}");
	}
}
