package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Profile("avsluttSaker")
@EnableTransactionManagement
@EntityScan(basePackages = {
	"no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities"
})
@EnableJpaRepositories(
		basePackages = {"no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository"
})
public class RepositoryConfig {
}
