package no.nav.dokarkivavlevering;

import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableConfigurationProperties(DokarkivavleveringProperties.class)
public class ApplicationConfig {
}
