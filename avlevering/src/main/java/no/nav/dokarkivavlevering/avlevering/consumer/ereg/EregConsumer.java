package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@Profile("genererAvlevering")
public class EregConsumer {

	public static final String FALLBACK_ORGNR_HAR_FEIL_FORMAT = "Ugyldig organisasjonsnummer";
	public static final String FALLBACK_ORGANISASJON_IKKE_FUNNET = "Ukjent organisasjonsnummer";
	public static final String FALLBACK_ORGANISASJON_MANGLER_NAVN = "Ukjent organisasjonsnavn";

	private final RestClient restClient;

	public EregConsumer(RestClient.Builder restClientBuilder,
	                    DokarkivavleveringProperties avleveringProperties) {
		this.restClient = restClientBuilder
				.baseUrl(avleveringProperties.getEndpoints().getEreg().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(includes = EregTechnicalException.class)
	public String hentOrganisasjonsnavn(String orgnr) {
		if (orgnrHarUgyldigFormat(orgnr)) {
			return FALLBACK_ORGNR_HAR_FEIL_FORMAT;
		}

		EregResponse response = restClient.get()
				.uri("/{orgnr}/noekkelinfo", orgnr)
				.exchange((_, res) -> handterRespons(orgnr, res));

		if (response == null) {
			return FALLBACK_ORGANISASJON_IKKE_FUNNET;
		} else if (response.navn() == null || response.navn().sammensattnavn() == null) {
			return FALLBACK_ORGANISASJON_MANGLER_NAVN;
		}

		return response.navn().sammensattnavn();
	}

	private static boolean orgnrHarUgyldigFormat(String orgnr) {
		return !(isNotBlank(orgnr) && orgnr.matches("^\\d{9}$"));
	}

	private static EregResponse handterRespons(String orgnr, ConvertibleClientHttpResponse res) throws IOException {
		if (NOT_FOUND.isSameCodeAs(res.getStatusCode())) {
			return null;
		}

		if (res.getStatusCode().is4xxClientError()) {
			throw new EregFunctionalException("Funksjonell feil ved kall mot Ereg for organisasjonsnummer=%s. status=%s".formatted(orgnr, res.getStatusCode()));
		}

		if (res.getStatusCode().is5xxServerError()) {
			throw new EregTechnicalException("Teknisk feil ved kall mot Ereg for organisasjonsnummer=%s. status=%s".formatted(orgnr, res.getStatusCode()));
		}

		return res.bodyTo(EregResponse.class);
	}

}