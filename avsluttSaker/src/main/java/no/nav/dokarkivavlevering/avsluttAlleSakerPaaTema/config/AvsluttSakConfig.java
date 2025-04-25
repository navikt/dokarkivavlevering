package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Profile("avsluttSaker")
@EnableConfigurationProperties(AvsluttSakProperties.class)
public class AvsluttSakConfig {
}