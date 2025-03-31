package no.nav.dokarkivavlevering.config;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakConfig;
import no.nav.dokarkivavlevering.core.CoreConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({
		CoreConfig.class,
		AvsluttSakConfig.class
})
@Profile("itest")
@EnableAutoConfiguration
public class ApplicationTestConfig {
}
