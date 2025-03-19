package no.nav.dokarkivavlevering.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@Validated
@ConfigurationProperties("dokarkivavlevering")
public class DokarkivavleveringProperties {

	public static final String BEHANDLINGSNUMMER = "B524";

	private final Endpoints endpoints = new Endpoints();

	private final String avleveringId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"));
	@NotEmpty
	String tema;
	@NotNull
	@Max(1000) // max IN query i Oracle
	int batchsize;
	@NotEmpty
	String ststokenurl;
	@NotEmpty
	String eregurl;
	@ToString.Exclude
	@NotEmpty
	String AsposeLicense;

	/**
	 * Brukes for å generere systemID for avlevering under oppstart. Referes til på forskjellige nivåer i arkivstruktur.xml.
	 */
	@Valid
	private final DokarkivavleveringProperties.ArkivConfig arkivConfig = new ArkivConfig();
	@Valid
	private final Activedirectory activedirectory = new Activedirectory();
	@Valid
	private final Filomraade filomraade = new Filomraade();
	@Valid
	private final Serviceuser serviceuser = new Serviceuser();
	@Valid
	private final Periode periode = new Periode();

	@Value
	@Valid
	public static class ArkivConfig {
		String systemID = UUID.randomUUID().toString();
		ArkivdelConfig arkivdelConfig = new ArkivdelConfig();
	}

	@Value
	@Valid
	public static class ArkivdelConfig {
		String systemID = UUID.randomUUID().toString();
	}

	@Data
	@Validated
	public static class Activedirectory {
		@NotEmpty
		private String basedn;
	}

	@Data
	@Validated
	public static class Filomraade {
		@NotEmpty
		private String work;
	}

	@Data
	@Validated
	public static class Serviceuser {
		@NotEmpty
		private String username;
		@NotEmpty
		@ToString.Exclude
		private String password;
	}

	@Data
	@Validated
	public static class Periode {
		@NotNull
		private LocalDate startdato;
		@NotNull
		private LocalDate sluttdato;
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
}
