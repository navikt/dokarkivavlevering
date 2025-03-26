package no.nav.dokarkivavlevering.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@Configuration
@EnableScheduling
public class CoreConfig {
}

