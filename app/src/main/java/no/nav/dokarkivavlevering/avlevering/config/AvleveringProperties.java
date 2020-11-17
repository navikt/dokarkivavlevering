package no.nav.dokarkivavlevering.avlevering.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Valid
@ConfigurationProperties("avlevering")
public class AvleveringProperties {

	@NotEmpty
	private String ststokenurl;
	@NotEmpty
	private String pdlurl;

	private final Serviceuser serviceuser = new Serviceuser();
	private final Periode periode = new Periode();

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
