package no.nav.dokarkivavlevering.avlevering.sftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.camel.LoggingLevel.INFO;

/**
 * Skriver filer sftp relativ til der avleveringspakken skal ligge.
 */
@Slf4j
@Component
public class AvleveringSFTPRoute extends RouteBuilder {

	public static final String SFTP = "direct:sftp";
	public static final String HEADER_FILNAVN = "AvleveringFilnavn";
	private static final String SFTP_ENDPOINT =
			"sftp://{{sftp.url}}:{{sftp.port}}/{{sftp.remote-file-path}}" +
					"?username={{sftp.username}}" +
					"&password=" +
					"&binary=true" +
					"&privateKeyFile={{sftp.private-key-file}}" +
					"&privateKeyPassphrase={{sftp.private-key-passphrase}}" +
					"&preferredAuthentications=publickey";

	@Override
	public void configure() throws Exception {
		from(SFTP)
				.routeId("sftp")
				.setHeader(FILE_NAME, simple("${exchangeProperty.AvleveringId}/avleveringspakke/${header." + HEADER_FILNAVN + "}"))
				.to(SFTP_ENDPOINT)
				.log(INFO, log, "Skrevet til fil={{sftp.remote-file-path}}/${header." + FILE_NAME + "} på {{sftp.url}}.");
	}
}
