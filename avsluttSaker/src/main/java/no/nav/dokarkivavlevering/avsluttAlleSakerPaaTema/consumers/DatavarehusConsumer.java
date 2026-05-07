package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers;

import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringFunctionalException;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringTechnicalException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Profile("avsluttSaker")
public class DatavarehusConsumer {

	private final RestClient restClient;
	private static final String DVH_QUERY = """
			{"mapping_node_type":{"$or":[{"$eq":"ARENAENHET"},{"$eq":"INFOENHET"},{"$eq":"NORGENHET"}]}}
			""";
	private static final int MAX_ANTALL_ENHETER_SOM_SKAL_HENTES = 100_000; // Per 7. mai 2026 er det 12676 administrative enheter

	public DatavarehusConsumer(DokarkivavleveringProperties dokarkivavleveringProperties,
							   RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
				.baseUrl(dokarkivavleveringProperties.getEndpoints().getDatavarehus().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = {DokarkivavleveringTechnicalException.class, RestClientException.class})
	public DatavarehusResponse hentAlleAdministrativeEnheter() {
		return restClient.get()
				.uri(uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
						.queryParam("q", DVH_QUERY)
						.queryParam("limit", MAX_ANTALL_ENHETER_SOM_SKAL_HENTES)
						.build()
						.toUri())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (_, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokarkivavleveringFunctionalException(format("Funksjonell feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
					}
					throw new DokarkivavleveringTechnicalException(format("Teknisk feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
				})
				.body(DatavarehusResponse.class);
	}
}
