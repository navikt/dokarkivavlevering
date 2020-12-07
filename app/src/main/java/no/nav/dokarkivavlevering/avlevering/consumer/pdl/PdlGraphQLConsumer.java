package no.nav.dokarkivavlevering.avlevering.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.consumer.sts.StsRestConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.Bruker;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * https://navikt.github.io/pdl
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String HEADER_PDL_TEMA = "Tema";

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
	public Map<String, Bruker> hentPersonBolk(final Set<String> aktoerIds, final String tema) {
		if(aktoerIds.isEmpty()) {
			return new HashMap<>();
		}
		try {
			final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build();
			final String serviceuserToken = "Bearer " + stsConsumer.getStsToken().getAccess_token();
			final RequestEntity<PdlRequest> requestEntity = RequestEntity.post(uri.toUri())
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header(HttpHeaders.AUTHORIZATION, serviceuserToken)
					.header(HEADER_PDL_NAV_CONSUMER_TOKEN, serviceuserToken)
					.header(HEADER_PDL_TEMA, tema)
					.body(mapRequest(aktoerIds));
			log.debug("Henter fødselsnummer og navn for {} aktørIds", aktoerIds.size());
			final PdlHentPersonBolkResponse pdlHentPersonBolkResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlHentPersonBolkResponse.class).getBody());
			log.debug("Ferdig hentet fødsesnummer og navn for {} aktørIds", aktoerIds.size());
			if (pdlHentPersonBolkResponse.getErrors() == null || pdlHentPersonBolkResponse.getErrors().isEmpty()) {
				return createResponseAsMap(pdlHentPersonBolkResponse.getData().getHentPersonBolk());
			} else {
				throw new PdlFunctionalException("Kunne ikke hente bolk identer fra pdl." + pdlHentPersonBolkResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente bolk identer fra pdl.", e);
		}
	}

	private Map<String, Bruker> createResponseAsMap(List<PdlHentPersonBolkResponse.PdlHentPersonBolk> hentPersonBolk) {
		return hentPersonBolk.stream().collect(Collectors.toMap(PdlHentPersonBolkResponse.PdlHentPersonBolk::getIdent,
				pdlHentPersonBolk -> new Bruker(pdlHentPersonBolk.getFolkeregisterIdent(), pdlHentPersonBolk.getFulltnavn())));
	}

	private PdlRequest mapRequest(final Set<String> aktoerIds) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("identer", aktoerIds);
		return PdlRequest.builder()
				.query("query hentIdenterBolk($identer: [ID!]!) {\n" +
						"  hentPersonBolk(identer: $identer) {\n" +
						"    ident\n" +
						"    person {\n" +
						"      folkeregisteridentifikator {\n" +
						"        identifikasjonsnummer\n" +
						"      }\n" +
						"      navn {\n" +
						"        fornavn\n" +
						"        mellomnavn\n" +
						"        etternavn\n" +
						"      }\n" +
						"    }\n" +
						"    code\n" +
						"  }\n" +
						"}\n")
				.variables(variables)
				.build();
	}
}
