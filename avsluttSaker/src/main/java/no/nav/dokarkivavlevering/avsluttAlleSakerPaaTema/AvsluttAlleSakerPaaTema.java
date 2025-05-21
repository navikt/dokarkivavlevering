package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.services.AvsluttAlleSakerService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.validators.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;

@Slf4j
@Component
@Profile("avsluttSaker")
public class AvsluttAlleSakerPaaTema {

	private final AvsluttSakProperties avsluttSakProperties;
	private final AvsluttAlleSakerService avsluttAlleSakerService;
	private final ApplicationContext context;

	public AvsluttAlleSakerPaaTema(AvsluttSakProperties avsluttSakProperties, AvsluttAlleSakerService avsluttAlleSakerService, ApplicationContext context) {
		this.avsluttSakProperties = avsluttSakProperties;
		this.avsluttAlleSakerService = avsluttAlleSakerService;
		this.context = context;
	}

	@Scheduled(initialDelay = 1000)
	public void execute() {
		String referanse = avsluttSakProperties.getReferanse();
		LocalDateTime avsluttetDato = avsluttSakProperties.getAvsluttetDato();
		String administrativEnhet = avsluttSakProperties.getAdministrativEnhet();

		log.info("avsluttAlleSakerPaaTema skal avslutte alle saker i arbeidstabellen med referanse={}, avsluttetDato={} og administrativEnhet={}", referanse, avsluttetDato, administrativEnhet);
		validerAvsluttAlleSakerPaaTemaRequest(referanse, avsluttetDato, administrativEnhet);

		avsluttAlleSakerService.avsluttAlleSaker();

		log.info("avsluttAlleSakerPaaTema har avsluttet alle saker i arbeidstabellen med referanse={}, avsluttetDato={} og administrativEnhet={}", referanse, avsluttetDato, administrativEnhet);
		System.exit(SpringApplication.exit(context, () -> 0));
	}
}
