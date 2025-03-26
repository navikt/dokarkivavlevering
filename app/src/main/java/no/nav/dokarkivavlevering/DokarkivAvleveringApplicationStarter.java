package no.nav.dokarkivavlevering;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.ProduserAvleveringspakkeTilArkivverket;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttAlleSakerPaaTema;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DokarkivAvleveringApplicationStarter {

	private final DokarkivavleveringProperties dokarkivavleveringProperties;

	private final AvsluttAlleSakerPaaTema avsluttAlleSakerPaaTema;
	private final ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket;

	public DokarkivAvleveringApplicationStarter(AvsluttAlleSakerPaaTema avsluttAlleSakerPaaTema,
												ProduserAvleveringspakkeTilArkivverket produserAvleveringspakkeTilArkivverket,
												DokarkivavleveringProperties dokarkivavleveringProperties) {
		this.dokarkivavleveringProperties = dokarkivavleveringProperties;
		this.avsluttAlleSakerPaaTema = avsluttAlleSakerPaaTema;
		this.produserAvleveringspakkeTilArkivverket = produserAvleveringspakkeTilArkivverket;
	}

	@Scheduled(initialDelay = 1000)
	public void startApplication() {
		log.info("Starter jobb: " + dokarkivavleveringProperties.getJobtype().name());
		switch (dokarkivavleveringProperties.getJobtype()) {
			case AVLEVERING -> produserAvleveringspakkeTilArkivverket.execute();
			case AVSLUTTSAKER -> avsluttAlleSakerPaaTema.execute();
		}
	}
}

