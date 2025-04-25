package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Validated
@Profile("avsluttSaker")
@ConfigurationProperties("avsluttsak")
public class AvsluttSakProperties {

	@NotEmpty
	String tema;

	@NotEmpty
	private String referanse;

	private LocalDateTime avsluttetDato;

	private String administrativEnhet;

	public LocalDateTime getAvsluttetDato(){
		return avsluttetDato != null ? avsluttetDato : now();
	}

}