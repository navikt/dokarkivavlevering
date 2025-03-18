package no.nav.dokarkivavlevering;


import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableConfigurationProperties(AvleveringProperties.class)
public class ApplicationConfig {
}
