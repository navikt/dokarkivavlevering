package no.nav.dokarkivavlevering.config;

import jakarta.persistence.EntityManager;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config.RepositoryConfig;
import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository.ArbeidssakRepository;
import no.nav.dokarkivavlevering.core.CoreConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
	protected ArbeidssakRepository arbeidssakRepository;

	@Autowired
	protected EntityManager entityManager;

	@BeforeEach
	public void cleanUp() {
		emptyDatabases();
	}

	protected void emptyDatabases() {
		arbeidssakRepository.deleteAll();
	}

	protected void commitAndBeginNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected static void stubTexas() {
		stubFor(post("/nais-texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("texas/texas-happy.json")));
	}

	protected static void stubTexasTomBody() {
		stubFor(post("/nais-texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("texas/tom-body.json")));
	}

	protected static void stubPdl(String filename) {
		stubFor(post("/pdl")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}

	protected static void stubDvh(String filename) {
		stubFor(get(urlPathEqualTo("/dvh"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dvh/" + filename)));
	}
}
