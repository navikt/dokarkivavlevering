package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerPaaTema {

	private final AvsluttSakProperties avsluttSakProperties;
	private final AvsluttAlleSakerService avsluttAlleSakerService;

	public AvsluttAlleSakerPaaTema(AvsluttSakProperties avsluttSakProperties, AvsluttAlleSakerService avsluttAlleSakerService) {
		this.avsluttSakProperties = avsluttSakProperties;
		this.avsluttAlleSakerService = avsluttAlleSakerService;
	}

	@Scheduled(initialDelay = 1000)
	public void execute(){
		String tema = avsluttSakProperties.getTema();
		String referanse = avsluttSakProperties.getReferanse();
		LocalDateTime avsluttetDato = avsluttSakProperties.getAvsluttetDato();
		String administrativEnhet = avsluttSakProperties.getAdministrativEnhet();

		validerAvsluttAlleSakerPaaTemaRequest(tema, referanse, avsluttetDato, administrativEnhet);
		log.info("avsluttAlleSakerPaaTema skal avslutte alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}", tema, referanse, avsluttetDato, administrativEnhet);
		avsluttAlleSakerService.avsluttAlleSaker();

	}
}
