package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArkivsakJournalpostRepository;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SakRepository;
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
		basePackageClasses = {
				SakRepository.class,
				ArkivsakJournalpostRepository.class
		})
public class RepositoryConfig {

}
