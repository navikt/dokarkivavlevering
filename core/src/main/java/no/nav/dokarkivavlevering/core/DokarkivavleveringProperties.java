package no.nav.dokarkivavlevering.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Data
@Validated
@ConfigurationProperties("dokarkivavlevering")
public class DokarkivavleveringProperties {

	/*
	 * Påkrevd config
	 */
	private final Endpoints endpoints = new Endpoints();
	@Valid
	private final Activedirectory activedirectory = new Activedirectory();
	@NotEmpty
	String eregurl;

	@ToString.Exclude
	@NotEmpty
	String AsposeLicense;

	@NotNull
	Jobtype jobtype;

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

	/*
	 * Generelle properties
	 */

	@NotEmpty
	String tema;

	@Max(1000) // max IN query i Oracle
	int batchsize;

	/*
	 * Properties for generering av arkivpakke til arkivverket
	 */

	private final Periode periode = new Periode();

	@Data
	public static class Periode {
		private LocalDate startdato;
		private LocalDate sluttdato;
	}

}
