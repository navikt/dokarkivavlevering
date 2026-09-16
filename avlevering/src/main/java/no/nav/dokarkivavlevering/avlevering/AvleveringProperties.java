package no.nav.dokarkivavlevering.avlevering;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

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

}