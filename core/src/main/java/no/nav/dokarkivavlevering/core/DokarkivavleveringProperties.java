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

	@Valid
	private final Endpoints endpoints = new Endpoints();

	@Valid
	private final Activedirectory activedirectory = new Activedirectory();

	@Data
	public static class Activedirectory {
		@NotEmpty
		private String basedn;
	}

	@Max(1000) // max IN query i Oracle
	int batchsize;

	@Data
	public static class Endpoints {
		@Valid
		@NotNull
		private AzureEndpoint pdl;

		@Valid
		@NotNull
		private Endpoint ereg;

		@Valid
		@NotNull
		private Endpoint datavarehus;
	}

	@Data
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
	public static class Endpoint {
		@NotEmpty
		private String url;
	}

}