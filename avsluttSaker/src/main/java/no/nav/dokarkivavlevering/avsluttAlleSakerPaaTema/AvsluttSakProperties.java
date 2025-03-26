package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.azure.AzureProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@Validated
@Profile("avsluttSak")
@ConfigurationProperties("avsluttsak")
public class AvsluttSakProperties {


	@NotEmpty
	String tema;
	@NotEmpty
	private String referanse;
	@NotEmpty
	private String administrativEnhet;
	private LocalDateTime avsluttetDato;

}

