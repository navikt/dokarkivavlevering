package no.nav.dokarkivavlevering.core;

import no.nav.dokarkivavlevering.core.azure.AzureProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
@EnableConfigurationProperties({DokarkivavleveringProperties.class, AzureProperties.class})
public class CoreConfig {
}
