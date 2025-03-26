package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerPaaTema {

	private final AvsluttSakProperties avsluttSakProperties;

	public AvsluttAlleSakerPaaTema(AvsluttSakProperties avsluttSakProperties) {
		this.avsluttSakProperties = avsluttSakProperties;
	}

	@Scheduled(initialDelay = 1000)
	public void execute(){
		validerAvsluttAlleSakerPaaTemaRequest(
				avsluttSakProperties.getTema(),
				avsluttSakProperties.getReferanse(),
				avsluttSakProperties.getAvsluttetDato(),
				avsluttSakProperties.getAdministrativEnhet());
		log.info("avsluttAlleSakerPaaTema skal avslutte alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}",
				avsluttSakProperties.getTema(), avsluttSakProperties.getReferanse(), avsluttSakProperties.getAvsluttetDato(), avsluttSakProperties.getAdministrativEnhet());
	}
}
