package no.nav.dokarkivavlevering.avlevering.config;

import lombok.Data;
import lombok.ToString;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Validated
@ConfigurationProperties("avlevering")
public class AvleveringProperties {

	private final String avleveringId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"));
	@NotEmpty
	private String tema;
	@NotNull
	@Max(1000) // max IN query i Oracle
	private Long batchsize;
	@NotEmpty
	private String ststokenurl;
	@NotEmpty
	private String pdlurl;
	@NotEmpty
	private String eregurl;
	@ToString.Exclude
	@NotEmpty
	private String AsposeLicense;

	/**
	 * Brukes for å generere systemID for avlevering under oppstart. Referes til på forskjellige nivåer i arkivstruktur.xml.
	 */
	@Valid
	private final AvleveringProperties.ArkivConfig arkivConfig = new ArkivConfig();
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
		private final String systemID = UUID.randomUUID().toString();
		private final ArkivdelConfig arkivdelConfig = new ArkivdelConfig();
	}

	@Value
	@Valid
	public static class ArkivdelConfig {
		private final String systemID = UUID.randomUUID().toString();
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
}
