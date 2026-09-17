package no.nav.dokarkivavlevering.config;

import no.nav.dokarkivavlevering.avlevering.AvleveringConfiguration;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
@EnableAutoConfiguration
@Import(AvleveringConfiguration.class)
@ComponentScan(basePackageClasses = AvleveringRepository.class)
public class ApplicationTestConfig {
}
