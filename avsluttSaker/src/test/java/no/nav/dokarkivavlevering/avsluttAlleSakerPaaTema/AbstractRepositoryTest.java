package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = RepositoryConfig.class)
@ActiveProfiles(profiles = {"avsluttSaker"})
public abstract class AbstractRepositoryTest {

	@Autowired
	protected ArbeidssakRepository arbeidssakRepository;

	@AfterEach
	protected void tearDown(){
		arbeidssakRepository.deleteAll();
	}

}