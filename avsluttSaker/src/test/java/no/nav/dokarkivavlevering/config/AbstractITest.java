package no.nav.dokarkivavlevering.config;

import jakarta.persistence.EntityManager;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SakRepository;
import no.nav.dokarkivavlevering.core.CoreConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Transactional
@SpringBootTest(
		classes = {
				ApplicationTestConfig.class,
				RepositoryConfig.class,
				CoreConfig.class},
		webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase
@AutoConfigureWireMock(port = DYNAMIC_PORT)
@ActiveProfiles(profiles = {"avsluttSaker", "itest"})
public abstract class AbstractITest {

	@Autowired
	protected SakRepository sakRepository;

	@Autowired
	public WebTestClient webTestClient;

	@Autowired
	protected EntityManager entityManager;

	@BeforeEach
	public void setUp() {
	}

	protected void emptyDatabases() {
		sakRepository.deleteAll();
	}

	protected void commitAndBeginNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

}
