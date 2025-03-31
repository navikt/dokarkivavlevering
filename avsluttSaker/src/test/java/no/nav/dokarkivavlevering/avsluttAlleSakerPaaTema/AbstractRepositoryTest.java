package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = RepositoryConfig.class)
@ActiveProfiles(profiles = {"avsluttSaker", "itest"})
public abstract class AbstractRepositoryTest {

	@Autowired
	SakRepository sakRepository;

}