package no.nav.dokarkivavlevering.config;

import jakarta.persistence.EntityManager;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.SakRepository;
import no.nav.dokarkivavlevering.core.CoreConfig;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

	@AfterEach
	public void setUp() {
		emptyDatabases();
	}

	protected void emptyDatabases() {
		sakRepository.deleteAll();
	}

	protected void commitAndBeginNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected static void stubPdl(String filename) {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}
}
