package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("avsluttSaker")
@Import(RepositoryConfig.class)
@EnableConfigurationProperties(AvsluttSakProperties.class)
public class AvsluttSakConfig {
}
