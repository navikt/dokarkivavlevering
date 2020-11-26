package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.loependejournal.AvleveringLoependeJournalRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Component
public class AvleveringRoute extends RouteBuilder {
	//static final Set<String> TEMA_AVLEVERES = Stream.of("ERS", "SAK", "KTR", "MED", "UFM", "RVE", "OPP", "SAP", "IAR").collect(Collectors.toSet());
	static final Set<String> TEMA_AVLEVERES = Stream.of("MED").collect(Collectors.toSet());
	public static final String PROPERTY_AVLEVERING_ID = "AvleveringId";
	public static final String PROPERTY_TEMA = "AvleveringTema";

	private final ApplicationContext springContext;
	private final AvleveringProperties avleveringProperties;

	@Autowired
	public AvleveringRoute(AvleveringProperties avleveringProperties,
						   ApplicationContext springContext) {
		this.avleveringProperties = avleveringProperties;
		this.springContext = springContext;
	}

	@Override
	public void configure() throws Exception {
		// feil som ikke blir håndtert i try-catches eller i onException gjør at appen blir skrudd av.
		errorHandler(deadLetterChannel("direct:shutdown")
				.log(log)
				.disableRedelivery()
				.loggingLevel(LoggingLevel.ERROR)
				.logHandled(true)
				.logExhausted(true)
				.logExhaustedMessageHistory(false));

		from("timer://runOnce?repeatCount=1&delay=1000")
				.routeId("start_avlevering")
				.setProperty(PROPERTY_AVLEVERING_ID, constant(avleveringProperties.getAvleveringId()))
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering starter avlevering=${exchangeProperty.AvleveringId}.")
				.log(LoggingLevel.INFO, log, "Konfigurasjon=" + avleveringProperties)
				.setBody(constant(TEMA_AVLEVERES))
				.split(body())
				.setProperty(PROPERTY_TEMA, body())
				.to("direct:behandle_tema")
				.end()
				.to(AvleveringStatiskRoute.AVLEVERING_STATIC)
				.to("direct:generer_arkivstruktur")
				.to(AvleveringLoependeJournalRoute.GENERER_LOEPENDEJOURNAL)
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering er ferdig med avlevering.")
				.to("direct:shutdown");

		from("direct:shutdown")
				.routeId("shutdown")
				.process(new Processor() {
					Thread stop;

					@Override
					public void process(final Exchange exchange) throws Exception {
						// stop this route using a thread that will stop
						// this route gracefully while we are still running
						if (stop == null) {
							stop = new Thread() {
								@Override
								public void run() {
									try {
										exchange.getContext().shutdown();
										SpringApplication.exit(springContext, () -> 0);
										System.exit(0);
									} catch (Exception e) {
										// ignore
									}
								}
							};
						}

						// start the thread that stops this route
						stop.start();
					}
				});
	}
}