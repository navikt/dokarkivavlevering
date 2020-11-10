package no.nav.dokarkivavlevering.avlevering;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AvleveringRoute extends RouteBuilder {
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
		from("timer://runOnce?repeatCount=1&delay=1000")
				.routeId("start_avlevering")
				.log(LoggingLevel.INFO, log, "Dokarkivavlevering starter avlevering.")
				.log(LoggingLevel.INFO, log, "Konfigurasjon=" + avleveringProperties)
				// Hent temaer
				// Start å behandle ett og ett tema
				.to("direct:behandle_tema")
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