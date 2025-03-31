package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan
@EnableJpaRepositories
@EnableTransactionManagement
@ActiveProfiles("avsluttSaker")
public class RepositoryConfig {
}
