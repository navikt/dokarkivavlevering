package no.nav.dokarkivavlevering.avlevering.arkivuttrekk;

import no.nav.dokarkivavlevering.avlevering.sftp.AvleveringSFTPRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("genererAvlevering")
public class AvleveringArkivuttrekkRoute extends RouteBuilder {

	public static final String GENERER_ARKIVUTTREKK = "direct:generer_arkivuttrekk";
	public static final String OPPRETT_ARKIVUTTREKK = "direct:opprett_arkivuttrekk";

	private final ArkivuttrekkMapper arkivuttrekkMapper;

	@Autowired
	public AvleveringArkivuttrekkRoute(ArkivuttrekkMapper arkivuttrekkMapper) {
		this.arkivuttrekkMapper = arkivuttrekkMapper;
	}

	@Override
	public void configure() {
		from(GENERER_ARKIVUTTREKK)
				.routeId("generer_arkivuttrekk")
				.log(LoggingLevel.INFO, log, "Starter generering av arkivuttrekk.xml")
				.to(OPPRETT_ARKIVUTTREKK)
				.log(LoggingLevel.INFO, log, "Ferdig med å generere arkivuttrekk.xml");

		from(OPPRETT_ARKIVUTTREKK)
				.routeId("opprett_arkivuttrekk")
				.bean(arkivuttrekkMapper)
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty.AvleveringId}/arkivuttrekk.xml"))
				.to("file://{{dokarkivavlevering.filomraade.work}}?fileExist=Override")
				.setHeader(AvleveringSFTPRoute.HEADER_FILNAVN, simple("arkivuttrekk.xml"))
				.to(AvleveringSFTPRoute.SFTP)
				.log(LoggingLevel.INFO, log, "Genererte arkivuttrekk til ${header.CamelFileNameProduced}");
	}
}
