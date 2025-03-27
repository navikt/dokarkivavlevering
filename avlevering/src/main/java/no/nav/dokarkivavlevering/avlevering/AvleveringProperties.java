package no.nav.dokarkivavlevering.avlevering;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Data
@Validated
@Profile("genererAvlevering")
@ConfigurationProperties("avlevering")
@EnableConfigurationProperties(AvleveringProperties.class)
public class AvleveringProperties {

	@ToString.Exclude
	@NotEmpty
	String asposeLicense;

	@NotEmpty
	String tema;
	@NotNull
	private final Periode periode = new Periode();

	@Data
	public static class Periode {
		@NotEmpty
		private LocalDate startdato;
		@NotEmpty
		private LocalDate sluttdato;
	}


}

