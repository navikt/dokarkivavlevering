package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Consumers;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.AvsluttSakProperties;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringFunctionalException;
import no.nav.dokarkivavlevering.core.exception.DokarkivavleveringTechnicalException;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Profile("avsluttSaker")
public class DatavarehusConsumer {

	private final RestClient restClient;

	public DatavarehusConsumer(RestClient.Builder restClientBuilder,
							   AvsluttSakProperties avsluttSakProperties) {
		this.restClient = restClientBuilder
				.baseUrl("https://dvh.adeo.no/ords/dvh/dt_kodeverk/dim_org")
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = DokarkivavleveringTechnicalException.class)
	public DatavarehusResponse hentNavnForAdministrativEnhet(String journalfoerendeEnhet, LocalDateTime journaldato){
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("q", buildQuery(journalfoerendeEnhet, journaldato))
						.build())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokarkivavleveringFunctionalException(format("Funksjonell feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
					}
					throw new DokarkivavleveringTechnicalException(format("Funksjonell feil ved henting av henting av navn for administrativ fra DVH. Feilmelding=%s", res.getStatusText()));
				})
				.body(DatavarehusResponse.class);
	}

	private String buildQuery(String journalfoerendeEnhet, LocalDateTime journaldato) {
		return """
				{
					"mapping_node_type":
					{
						"$or":
							[
								{"$eq":"ARENAENHET"},
								{"$eq":"INFOENHET"},
								{"$eq":"NORGENHET"}
							]
					},
					"mapping_node_kode":"%s",
					"funk_gyldig_fra_dato":
					{
						"$lte":
						{"$date":"%s"}
					},
					"funk_gyldig_til_dato":
					{
						"$gte":
						{"$date":"%s"}
					}
				}
				""".formatted(
				journalfoerendeEnhet,
				journaldato,
				journaldato
				);
	}
}
