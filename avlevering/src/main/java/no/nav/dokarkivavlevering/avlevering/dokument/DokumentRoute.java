package no.nav.dokarkivavlevering.avlevering.dokument;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute.HEADER_FILNAVN;
import static no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute.SFTP;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentRoute extends RouteBuilder {

	public static final String SEND_DOKUMENT = "direct:send_dokument";
	private final DokumentMapper dokumentMapper;

	public DokumentRoute(DokumentMapper dokumentMapper) {
		this.dokumentMapper = dokumentMapper;
	}

	@Override
	public void configure() throws Exception {
		from(SEND_DOKUMENT)
				.routeId("send_dokument")
				.bean(dokumentMapper)
				.split(body())
				.setHeader(HEADER_FILNAVN, simple("DOKUMENTER/${exchangeProperty.AvleveringTema}/${body.journalpostId}_${body.filUuid}.pdf"))
				.setBody(simple("${body.fil}"))
				.to(SFTP)
				.end();
	}
}
