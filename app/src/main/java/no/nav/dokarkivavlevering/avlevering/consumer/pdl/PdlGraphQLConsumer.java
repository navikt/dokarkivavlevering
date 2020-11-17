package no.nav.dokarkivavlevering.avlevering.consumer.pdl;

import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.consumer.sts.StsRestConsumer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * https://navikt.github.io/pdl
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class PdlGraphQLConsumer {
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsConsumer;
	private final String pdlUrl;

	public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
							  StsRestConsumer stsConsumer,
							  AvleveringProperties avleveringProperties) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.build();
		this.stsConsumer = stsConsumer;
		this.pdlUrl = avleveringProperties.getPdlurl();
	}

	@Retryable(include = HttpServerErrorException.class)
	public List<PdlHentIdenterBolkResponse.PdlHentIdenterBolk> hentIdenterBolk(final Set<String> aktoerIds) {
		try {
			final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build();
			final String serviceuserToken = "Bearer " + stsConsumer.getStsToken().getAccess_token();
			final RequestEntity<PdlRequest> requestEntity = RequestEntity.post(uri.toUri())
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header(HttpHeaders.AUTHORIZATION, serviceuserToken)
					.header(HEADER_PDL_NAV_CONSUMER_TOKEN, serviceuserToken)
					.body(mapRequest(aktoerIds));
			final PdlHentIdenterBolkResponse pdlHentIdenterBolkResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlHentIdenterBolkResponse.class).getBody());

			if (pdlHentIdenterBolkResponse.getErrors() == null || pdlHentIdenterBolkResponse.getErrors().isEmpty()) {
				return pdlHentIdenterBolkResponse.getData().getHentIdenterBolk();
			} else {
				throw new PdlFunctionalException("Kunne ikke hente bolk identer fra pdl." + pdlHentIdenterBolkResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente bolk identer fra pdl.", e);
		}
	}

	private PdlRequest mapRequest(final Set<String> aktoerIds) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("identer", aktoerIds);
		return PdlRequest.builder()
				.query("query hentIdenterBolk($identer: [ID!]!) {\n" +
						"  hentIdenterBolk(identer: $identer, grupper: FOLKEREGISTERIDENT, historikk: false) {\n" +
						"    ident\n" +
						"    identer {\n" +
						"      ident\n" +
						"      gruppe\n" +
						"    }\n" +
						"  }\n" +
						"}\n")
				.variables(variables)
				.build();
	}
}
