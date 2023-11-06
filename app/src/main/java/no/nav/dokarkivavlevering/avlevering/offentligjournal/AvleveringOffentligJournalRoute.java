package no.nav.dokarkivavlevering.avlevering.offentligjournal;

import no.arkivverket.standarder.noark5.offentligjournal.Journalregistrering;
import no.arkivverket.standarder.noark5.offentligjournal.ObjectFactory;
import no.arkivverket.standarder.noark5.offentligjournal.OffentligJournal;
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
import java.util.List;

@Component
public class AvleveringOffentligJournalRoute extends RouteBuilder {
	public static final String HEADER_XSL_PARAM_OFFENTLIGJOURNAL_XML = "offentligjournal_xml";
	public static final String GENERER_OFFENTLIGJOURNAL = "direct:generer_offentligjournal";
	public static final String OPPRETT_OFFENTLIGJOURNAL_PRE = "direct:opprett_offentligjournal_pre";
	public static final String FLETT_OFFENTLIGJOURNAL = "direct:flett_offentligjournal";
	public static final String OFFENTLIGJOURNAL = "direct:offentligjournal";

	private JaxbDataFormat marshalJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		jaxbDataFormat.setSchemaLocation("http://www.arkivverket.no/standarder/noark5/offentligJournal offentligJournal.xsd");
		return jaxbDataFormat;
	}

	private final JournalRegistreringService journalregistreringService;
	private final OffentligJournalMapper offentligJournalMapper;

	public AvleveringOffentligJournalRoute(JournalRegistreringService journalregistreringService, OffentligJournalMapper offentligJournalMapper) {
		this.journalregistreringService = journalregistreringService;
		this.offentligJournalMapper = offentligJournalMapper;
	}

	@Override
	public void configure() throws Exception {
		from(GENERER_OFFENTLIGJOURNAL)
				.routeId("generer_offentligjournal")
				.log(LoggingLevel.INFO, log, "Starter generering av offentligJournal.xml.")
				.to(OPPRETT_OFFENTLIGJOURNAL_PRE)
				.to(FLETT_OFFENTLIGJOURNAL)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere offentligJournal.xml.");

		from(OPPRETT_OFFENTLIGJOURNAL_PRE)
				.routeId("opprett_offentligjournal_pre")
				.log(LoggingLevel.INFO, log, "offentligjournal_pre")
				.bean(offentligJournalMapper)
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/pre_offentligjournal.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte pre_offentligjournal til ${header.CamelFileNameProduced}");

		from(FLETT_OFFENTLIGJOURNAL)
				.routeId("flett_offentligjournal")
				.log(LoggingLevel.INFO, log, "flett_offentligjournal")
				.process(exchange -> {
					// Fra opprett_offentligjournal_pre
					InputStream inputStream = FileUtils.openInputStream(Paths.get(exchange.getIn().getHeader(Exchange.FILE_NAME_PRODUCED, String.class)).toFile());
					exchange.getIn().setBody(inputStream);
				})
				.setHeader(HEADER_XSL_PARAM_OFFENTLIGJOURNAL_XML, simple("file:///{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/?select=offentligjournal_*.xml"))
				.setHeader(Exchange.XSLT_FILE_NAME, simple("{{avlevering.filomraade.work}}/${exchangeProperty.AvleveringId}/offentligJournal.xml"))
				.to("xslt:classpath:offentligjournal/embed_registrering_into_offentligjournal.xsl?output=file")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("offentligJournal.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Genererte offentligjournal til ${header.CamelXsltFileName}");


		from(OFFENTLIGJOURNAL)
				.routeId("offentligJournal")
				.log(LoggingLevel.INFO, log, "RouteID: offentligjournal")
				.log(LoggingLevel.INFO, log, "Behandler offentligjournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}")
				.bean(journalregistreringService)
				.process(exchange -> {
					OffentligJournal journal = new OffentligJournal();
					journal.getJournalregistrerings().addAll((List<Journalregistrering>) exchange.getIn().getBody());
					exchange.getIn().setBody(journal);
				})
				.marshal(marshalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/offentligjournal_${exchangeProperty.AvleveringTema}_${header.camelSplitIndex}.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Append")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig ${header.CamelFilenameProduced} for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelSplitIndex}");
	}
}
