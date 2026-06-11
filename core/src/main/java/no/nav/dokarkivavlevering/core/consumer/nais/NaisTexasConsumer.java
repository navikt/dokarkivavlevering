package no.nav.dokarkivavlevering.core.consumer.nais;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.consumer.nais.exception.TomBodyTexasException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Slf4j
@Component
public class NaisTexasConsumer {

	private static final String ENTRA_ID = "entra_id";
	private final RestClient restClient;

	public NaisTexasConsumer(RestClient.Builder restClientBuilder, NaisProperties naisProperties) {
		this.restClient = restClientBuilder
				.baseUrl(naisProperties.tokenEndpoint())
				.build();
	}

	public String getSystemToken(String targetScope) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", ENTRA_ID);
		formData.add("target", targetScope);

		return Optional.ofNullable(restClient.post()
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class))
				.map(NaisTexasToken::accessToken)
				.orElseThrow(() -> new TomBodyTexasException("Tom body i token fra EntraId"));
	}

}