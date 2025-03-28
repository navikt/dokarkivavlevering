package no.nav.dokarkivavlevering.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("dokarkivavlevering")
public class DokarkivavleveringProperties {

	private final Endpoints endpoints = new Endpoints();

	@Valid
	private final Activedirectory activedirectory = new Activedirectory();

	@Data
	@Validated
	public static class Activedirectory {
		@NotEmpty
		private String basedn;
	}

	@Max(1000) // max IN query i Oracle
	int batchsize;

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AzureEndpoint pdl;

		@NotEmpty
		private Endpoint ereg;
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

	@Data
	@Validated
	public static class Endpoint {
		@NotEmpty
		private String url;
	}

}