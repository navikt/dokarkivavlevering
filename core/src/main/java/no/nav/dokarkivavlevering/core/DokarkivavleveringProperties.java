package no.nav.dokarkivavlevering.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Validated
@ConfigurationProperties("dokarkivavlevering")
public class DokarkivavleveringProperties {

	private final Endpoints endpoints = new Endpoints();
	@Valid
	private final Activedirectory activedirectory = new Activedirectory();
	@NotEmpty
	String eregurl;

	@Data
	@Validated
	public static class Activedirectory {
		@NotEmpty
		private String basedn;
	}

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AzureEndpoint pdl;
	}

	@Data
	@Validated
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;
		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}

	@Max(1000) // max IN query i Oracle
	int batchsize;


}
