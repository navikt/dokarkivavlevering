package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Avleverer statiske data som .xsd til sftp området.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
@Profile("genererAvlevering")
public class AvleveringStatiskRoute extends RouteBuilder {
	public static String AVLEVERING_STATIC = "direct:avlevering_statisk";

	@Override
	public void configure() throws Exception {
		from(AVLEVERING_STATIC)
				.routeId("avlevering_statisk")
				.process(readFromClasspath("arkivstruktur.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
				.process(readFromClasspath("endringslogg.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
				.process(readFromClasspath("loependeJournal.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
				.process(readFromClasspath("metadatakatalog.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
				.process(readFromClasspath("offentligJournal.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
				.process(readFromClasspath("addml.xsd"))
				.to(AvleveringSFTPRoute.SFTP)
		;
	}

	private Processor readFromClasspath(final String filename) {
		return new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				final InputStream resourceInputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
				exchange.getIn().setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, filename);
				exchange.getIn().setBody(resourceInputStream);
			}
		};
	}
}
