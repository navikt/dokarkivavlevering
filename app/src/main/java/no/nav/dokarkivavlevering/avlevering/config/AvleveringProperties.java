package no.nav.dokarkivavlevering.avlevering.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Valid
@ConfigurationProperties("avlevering")
public class AvleveringProperties {

	private final String avleveringId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"));

	@NotEmpty
	private String ststokenurl;
	@NotEmpty
	private String pdlurl;


	@Valid
	private final Activedirectory activedirectory = new Activedirectory();
	@Valid
	private final Filomraade filomraade = new Filomraade();
	@Valid
	private final Serviceuser serviceuser = new Serviceuser();
	@Valid
	private final Periode periode = new Periode();

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
		@NotEmpty
		private LocalDate startdato;
		@NotEmpty
		private LocalDate sluttdato;
	}
}
