package no.nav.dokarkivavlevering.core.consumer.pdl;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.Ident;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlHentPersonBolkResponse.PdlHentPersonBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlHentPersonBolkResponse.PdlNavn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;

@WireMockTest
class PdlGraphQLConsumerTest {

	private static final String PDL_PATH = "/pdl";
	private static final String PDL_SCOPE = "api://test-fss.pdl.pdl-api/.default";

	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wireMockInfo) {
		var pdl = new DokarkivavleveringProperties.AzureEndpoint();
		pdl.setUrl(wireMockInfo.getHttpBaseUrl().concat(PDL_PATH));
		pdl.setScope(PDL_SCOPE);

		var properties = new DokarkivavleveringProperties();
		properties.getEndpoints().setPdl(pdl);

		pdlGraphQLConsumer = new PdlGraphQLConsumer(restClient(), properties);
	}

	private static RestClient restClient() {
		var jsonMapper = JsonMapper.builder()
				.disable(FAIL_ON_NULL_FOR_PRIMITIVES)
				.build();

		return RestClient.builder()
				.configureMessageConverters(converters -> converters.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper)))
				.build();
	}

	@Nested
	class HentPersonBolkTest {
		@Test
		void skalHentePersoner() {
			stubPdl("hentpersonbolk-happy.json");

			List<PdlHentPersonBolk> personer = pdlGraphQLConsumer.hentPersonBolk(Set.of("17629124853", "45457208085"));

			assertThat(personer).extracting("code").containsOnly("ok");
			var navneliste = personer.stream().map(PdlHentPersonBolk::getPerson).map(PdlHentPersonBolkResponse.PdlPerson::getNavn).map(List::getFirst);
			assertThat(navneliste)
					.hasSize(2)
					.extracting(PdlNavn::getFornavn, PdlNavn::getMellomnavn, PdlNavn::getEtternavn)
					.containsExactly(
							tuple("FORNUFTIG", null, "ALKOVE"),
							tuple("UTGÅTT", null, "GANG")
					);
		}

		// I prod er flagget spring.jackson.deserialization.fail-on-null-for-primitives=false satt i application.properties for å påvirke json-deserializeringen.
		// Siden PdlGraphQLConsumerTest ikke er en SB-test vil ikke flagget bli plukket opp, så det må konfigureres i restClient.
		// NB: Testen vil kjøre grønt uavhengig av konfigen. i application.properties.
		@Test
		void skalTillateNullForPrimitiver() {
			stubPdl("hentpersonbolk-null-i-primitiver.json");

			List<PdlHentPersonBolk> personer = pdlGraphQLConsumer.hentPersonBolk(Set.of("45457208085"));

			assertThat(personer).extracting("code").containsOnly("ok");
			PdlNavn navn = personer.getFirst().getPerson().getNavn().getFirst();
			assertThat(navn.getFolkeregistermetadata().getSekvens()).isEqualTo(0);
			assertThat(navn.getMetadata().isHistorisk()).isFalse();
		}
	}

	@Nested
	class HentIdenterBolkTest {
		@Test
		void skalHentIdenter() {
			stubPdl("hentidenterbolk-happy.json");

			List<HentIdenterBolk> identer = pdlGraphQLConsumer.hentGjeldendeAktoerIder(Set.of("17629124853", "45457208085", "23459032983"));

			assertThat(identer).extracting("code").containsOnly("ok");
			assertThat(identer.stream()
					.map(HentIdenterBolk::getIdenter)
					.map(List::getFirst)
					.map(Ident::getIdent)
					.toList()
			).containsExactlyElementsOf(List.of("2145823582221", "2545830568483", "2212342629997"));
		}
	}

	private static void stubPdl(String filename) {
		stubFor(post(urlEqualTo(PDL_PATH))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}

}