package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.stereotype.Component;

import static no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakerValidator.validerAvsluttAlleSakerPaaTemaRequest;

@Slf4j
@Component
public class AvsluttAlleSakerPaaTema {

	private final DokarkivavleveringProperties avsluttSakProperties;

	public AvsluttAlleSakerPaaTema(DokarkivavleveringProperties dokarkivavleveringProperties) {
		this.avsluttSakProperties = dokarkivavleveringProperties;
	}

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
