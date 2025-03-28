package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

//@EntityScan(basePackages = "no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities")
@Configuration
@Profile("avsluttSaker")
@EnableConfigurationProperties(AvsluttSakProperties.class)
public class AvsluttSakConfiguration {
}
