package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.consumers;

import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringFunctionalException;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringTechnicalException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Profile("avsluttSaker")
public class DatavarehusConsumer {

	private final RestClient restClient;
	private final String dvhUrl;

	public DatavarehusConsumer(DokarkivavleveringProperties dokarkivavleveringProperties) {
		RestClient restClient = RestClient.create();
		dvhUrl = dokarkivavleveringProperties.getEndpoints().getDatavarehus().getUrl();
		this.restClient = restClient.mutate()
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = DokarkivavleveringTechnicalException.class)
	public DatavarehusResponse hentAlleAdministrativeEnheter() {
		var uri = UriComponentsBuilder
				.fromUriString(dvhUrl)
				.queryParam("q", buildQuery())
				.build().toUri();

		return restClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokarkivavleveringFunctionalException(format("Funksjonell feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
					}
					throw new DokarkivavleveringTechnicalException(format("Funksjonell feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
				})
				.body(DatavarehusResponse.class);
	}

	private String buildQuery() {
		return """
				{"mapping_node_type":{"$or":[{"$eq":"ARENAENHET"},{"$eq":"INFOENHET"},{"$eq":"NORGENHET"}]}}
				""";
	}
}
