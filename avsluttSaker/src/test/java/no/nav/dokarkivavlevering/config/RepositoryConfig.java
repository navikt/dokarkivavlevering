package no.nav.dokarkivavlevering.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan("no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema")
@EnableJpaRepositories("no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema")
@EnableTransactionManagement
@ActiveProfiles("avsluttSaker")
public class RepositoryConfig {
}
