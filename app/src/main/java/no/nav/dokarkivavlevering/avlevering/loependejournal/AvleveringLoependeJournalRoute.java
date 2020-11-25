package no.nav.dokarkivavlevering.avlevering.loependejournal;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Mappe;
import no.arkivverket.standarder.noark5.arkivstruktur.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.arkivverket.standarder.noark5.loependejournal.Journalregistrering;
import no.arkivverket.standarder.noark5.loependejournal.LoependeJournal;
import no.nav.dokarkivavlevering.avlevering.AvleveringRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AvleveringLoependeJournalRoute extends RouteBuilder {

	private JaxbDataFormat loependeJournalJaxbFormat() throws JAXBException {
		final JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setContext(JAXBContext.newInstance(ObjectFactory.class));
		return jaxbDataFormat;
	}


	private JaxbDataFormat journalRegistreringArkivstrukturJaxb() {
		JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(ObjectFactory.class.getPackage().getName());
		jaxbDataFormat.setEncoding(StandardCharsets.UTF_8.toString());
		jaxbDataFormat.setFragment(true);
		jaxbDataFormat.setPartClass(Journalregistrering.class);
		return jaxbDataFormat;
	}

	private RegistreringService registreringService;

	public AvleveringLoependeJournalRoute(RegistreringService registreringService) {
		this.registreringService = registreringService;
	}

	@Override
	public void configure() throws Exception {
		from("direct:loependeJournal")
				.routeId("loependeJournal")
				.log(LoggingLevel.INFO, log, "Behandler loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}")
				.bean(registreringService)
				.marshal(journalRegistreringArkivstrukturJaxb())
				.process(exchange -> {
					LoependeJournal journal = new LoependeJournal();
					journal.getJournalregistrerings().addAll((List<Journalregistrering>)exchange.getIn().getExchange().getIn().getBody());
					//journal.set(exchange.getProperty(AvleveringRoute.PROPERTY_TEMA, String.class));
					exchange.getIn().setBody(journal);
				})
				.setHeader(JaxbConstants.JAXB_PART_NAMESPACE, simple("{http://www.arkivverket.no/standarder/noark5/arkivstruktur}loependejournal"))
				.marshal(journalRegistreringArkivstrukturJaxb())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/LoependeJournal_${exchangeProperty.AvleveringTema}${header.CamelLoopIndex.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Append")
				.log(LoggingLevel.INFO, log, "Behandlet ferdig loependeJournal.xml for tema=${exchangeProperty.AvleveringTema}, loop=${header.CamelLoopIndex}");

		from("direct:opprett_loependeJournal_pre")
				.routeId("opprett_loependeJournal_pre")
				.bean(loependejournalMapper)
				.marshal(loependeJournalJaxbFormat())
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/loependeJournal_pre.xml"))
				.to("file://{{avlevering.filomraade.work}}?fileExist=Override")
				.log(LoggingLevel.INFO, log, "Genererte arkivstruktur til ${header.CamelFileNameProduced}");
	}
}
