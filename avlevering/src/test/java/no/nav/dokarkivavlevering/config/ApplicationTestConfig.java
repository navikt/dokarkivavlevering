package no.nav.dokarkivavlevering.config;

import no.nav.dokarkivavlevering.avlevering.AvleveringConfiguration;
import no.nav.dokarkivavlevering.avlevering.repository.AvleveringRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Minimal test-context som kun kobler inn AvleveringRepository (og AvleveringProperties
 * via AvleveringConfiguration) mot en embedded H2-database, uten å dra inn Camel-ruter,
 * LDAP, Aspose o.l. slik AvleveringRouteITest gjør.
 */
@Configuration
@Profile("itest")
@EnableAutoConfiguration
@Import(AvleveringConfiguration.class)
@ComponentScan(basePackageClasses = AvleveringRepository.class)
public class ApplicationTestConfig {
}
