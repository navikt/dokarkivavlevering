package no.nav.dokarkivavlevering;

import no.nav.dokarkivavlevering.config.ServiceuserAlias;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableConfigurationProperties({ServiceuserAlias.class})
@EnableRetry
public class AppConfig {
}
