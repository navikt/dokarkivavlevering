package no.nav.dokarkivavlevering.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestConfiguration
public class WiremockEarlyStubConfig {

	@Bean(initMethod = "start", destroyMethod = "stop")
	@Primary
	public WireMockServer wireMockServer() {
		return new WireMockServer(WireMockConfiguration.options().dynamicPort());
	}

	@Component
	public static class WireMockStubInitializer {

		private final WireMockServer wireMockServer;

		public WireMockStubInitializer(WireMockServer wireMockServer) {
			this.wireMockServer = wireMockServer;
		}

		@Order(1)
		@EventListener(ContextRefreshedEvent.class)
		public void setupStubs() {
			wireMockServer.stubFor(get(urlPathEqualTo("/dvh"))
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBodyFile("dvh/" + "response.json")));
		}
	}
}
