package no.nav.dokarkivavlevering.avlevering;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("genererAvlevering")
@EnableConfigurationProperties(AvleveringProperties.class)
public class AvleveringConfiguration {
}
