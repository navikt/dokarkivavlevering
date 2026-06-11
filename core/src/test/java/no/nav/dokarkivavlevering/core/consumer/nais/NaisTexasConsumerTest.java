package no.nav.dokarkivavlevering.core.consumer.nais;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.dokarkivavlevering.core.consumer.nais.exception.TomBodyTexasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@WireMockTest
class NaisTexasConsumerTest {

	private static final String GYLDIG_TARGET_SCOPE = "api://dev-fss.pdl.pdl-api/.default";

	private NaisTexasConsumer naisTexasConsumer;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wireMockInfo) {
		var naisProperties = new NaisProperties(wireMockInfo.getHttpBaseUrl().concat("/texas"));

		naisTexasConsumer = new NaisTexasConsumer(RestClient.builder(), naisProperties);
	}

	@Test
	void skalReturnereGyldigToken() {
		stubTexas();

		var token = naisTexasConsumer.getSystemToken(GYLDIG_TARGET_SCOPE);

		assertThat(token).isEqualTo("yeehaw");
	}

	@Test
	void skalKasteTomBodyTexasExceptionForTomBodyFraTexas() {
		stubTexasNullBody();

		assertThatExceptionOfType(TomBodyTexasException.class)
				.isThrownBy(() -> naisTexasConsumer.getSystemToken(GYLDIG_TARGET_SCOPE))
				.withMessage("Tom body i token fra EntraId");
	}

	private static void stubTexas() {
		stubFor(post("/texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("texas/texas-happy.json")));
	}

	private static void stubTexasNullBody() {
		stubFor(post("/texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("texas/tom-body.json")));
	}

}