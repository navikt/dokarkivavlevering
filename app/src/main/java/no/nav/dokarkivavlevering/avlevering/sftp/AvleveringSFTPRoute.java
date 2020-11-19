package no.nav.dokarkivavlevering.avlevering.sftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Skriver filer sftp relativ til der avleveringspakken skal ligge.
 */
@Slf4j
@Component
public class AvleveringSFTPRoute extends RouteBuilder {

	public static final String SFTP = "direct:sftp";
	public static final String HEADER_FILNAVN = "AvleveringFilnavn";
	private static final String SFTP_ENDPOINT =
			"sftp://{{sftp.url}}:{{sftp.port}}/{{sftp.remoteFilePath}}" +
					"?username={{sftp.username}}" +
					"&password=" +
					"&binary=true" +
					"&privateKeyFile={{sftp.privateKeyFile}}" +
					"&privateKeyPassphrase={{sftp.privateKeyPassphrase}}" +
					"&preferredAuthentications=publickey";

	@Override
	public void configure() throws Exception {
		from(SFTP)
				.routeId("sftp")
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/avleveringspakke/${header." + HEADER_FILNAVN + "}"))
				.to(SFTP_ENDPOINT)
				.log(LoggingLevel.INFO, log, "Skrevet til fil={{sftp.remoteFilePath}}/${header." + Exchange.FILE_NAME + "} på {{sftp.url}}.");
	}
}
