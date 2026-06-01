package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkivavlevering.avlevering.consumer.ereg.EregConsumer.FALLBACK_ORGANISASJON_IKKE_FUNNET;
import static no.nav.dokarkivavlevering.avlevering.consumer.ereg.EregConsumer.FALLBACK_ORGANISASJON_MANGLER_NAVN;
import static no.nav.dokarkivavlevering.avlevering.consumer.ereg.EregConsumer.FALLBACK_ORGNR_HAR_FEIL_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@WireMockTest
class EregServiceTest {

	private EregConsumer eregConsumer;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wireMockInfo) {
		var endpoint = new DokarkivavleveringProperties.Endpoint();
		endpoint.setUrl(wireMockInfo.getHttpBaseUrl().concat("/ereg"));

		var properties = new DokarkivavleveringProperties();
		properties.getEndpoints().setEreg(endpoint);

		eregConsumer = new EregConsumer(RestClient.builder(), properties);
	}

	private static final String EREG_URL = "/ereg/%s/noekkelinfo";
	private static final String GYLDIG_ORG_NR = "321654987";
	private static final String GYLDIG_ORGANISASJONSNAVN = "ANDERS ANDERSENS ELEKTROVERKSTE D";
	private static final String ORG_NR_SOM_IKKE_EKSISTERER = "987654321";
	private static final String ORG_NR_SOM_GIR_4XX_ELLER_5XX = "123456789";
	private static final String ORG_SOM_MANGLER_NAVN = "111222333";
	private static final String ORG_SOM_MANGLER_SAMMENSATTNAVN = "222333444";

	@Test
	void skalReturnereSammensattNavnVedGyldigOrgnr() {
		stubEreg();

		var organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(GYLDIG_ORG_NR);

		assertThat(organisasjonsnavn).isEqualTo(GYLDIG_ORGANISASJONSNAVN);
	}

	@ParameterizedTest
	@ValueSource(strings = {"1234", "1234567890", " ", "12345678a"})
	@NullAndEmptySource
	void skalReturnereUgyldigOrganisasjonsnummerVedFeilFormat(String orgnr) {
		var organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(orgnr);

		assertThat(organisasjonsnavn).isEqualTo(FALLBACK_ORGNR_HAR_FEIL_FORMAT);
	}

	@Test
	void skalReturnereUkjentOrganisasjonsnavnDersomNavnErNull() {
		stubEregNavnMangler();

		var organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(ORG_SOM_MANGLER_NAVN);

		assertThat(organisasjonsnavn).isEqualTo(FALLBACK_ORGANISASJON_MANGLER_NAVN);
	}

	@Test
	void skalReturnereUkjentOrganisasjonsnavnDersomSammensattnavnErNull() {
		stubEregSammensattnavnMangler();

		var organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(ORG_SOM_MANGLER_SAMMENSATTNAVN);

		assertThat(organisasjonsnavn).isEqualTo(FALLBACK_ORGANISASJON_MANGLER_NAVN);
	}

	@Test
	void skalReturnereUkjentOrganisasjonsnummerFor404NotFound() {
		stubEregNotFound();

		var organisasjonsnavn = eregConsumer.hentOrganisasjonsnavn(ORG_NR_SOM_IKKE_EKSISTERER);

		assertThat(organisasjonsnavn).isEqualTo(FALLBACK_ORGANISASJON_IKKE_FUNNET);
	}

	@Test
	void skalKasteEregFunctionalExceptionForAndre4xxFeil() {
		stubEreg(BAD_REQUEST);

		assertThatExceptionOfType(EregFunctionalException.class)
				.isThrownBy(() -> eregConsumer.hentOrganisasjonsnavn(ORG_NR_SOM_GIR_4XX_ELLER_5XX))
						.withMessage("Funksjonell feil ved kall mot Ereg for organisasjonsnummer=%s. status=400 BAD_REQUEST".formatted(ORG_NR_SOM_GIR_4XX_ELLER_5XX));
	}

	@Test
	void skalKasteEregTechnicalExceptionFor5xxFeil() {
		stubEreg(INTERNAL_SERVER_ERROR);

		assertThatExceptionOfType(EregTechnicalException.class)
				.isThrownBy(() -> eregConsumer.hentOrganisasjonsnavn(ORG_NR_SOM_GIR_4XX_ELLER_5XX))
				.withMessage("Teknisk feil ved kall mot Ereg for organisasjonsnummer=%s. status=500 INTERNAL_SERVER_ERROR".formatted(ORG_NR_SOM_GIR_4XX_ELLER_5XX));
	}

	private static void stubEreg() {
		stubFor(get(urlEqualTo(EREG_URL.formatted(GYLDIG_ORG_NR)))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("ereg/response.json")));
	}

	private static void stubEregNavnMangler() {
		stubFor(get(urlEqualTo(EREG_URL.formatted(ORG_SOM_MANGLER_NAVN)))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("ereg/navn_mangler.json")));
	}

	private static void stubEregSammensattnavnMangler() {
		stubFor(get(urlEqualTo(EREG_URL.formatted(ORG_SOM_MANGLER_SAMMENSATTNAVN)))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("ereg/sammensattnavn_mangler.json")));
	}

	private static void stubEregNotFound() {
		stubFor(get(urlEqualTo(EREG_URL.formatted(ORG_NR_SOM_IKKE_EKSISTERER)))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())));
	}

	private static void stubEreg(HttpStatus httpStatus) {
		stubFor(get(urlEqualTo(EREG_URL.formatted(ORG_NR_SOM_GIR_4XX_ELLER_5XX)))
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

}