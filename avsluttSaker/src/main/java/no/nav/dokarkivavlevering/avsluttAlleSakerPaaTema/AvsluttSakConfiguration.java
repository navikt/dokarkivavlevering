package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("avsluttSak")
@EnableConfigurationProperties(AvsluttSakProperties.class)
public class AvsluttSakConfiguration {
}
