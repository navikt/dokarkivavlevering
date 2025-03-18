package no.nav.dokarkivavlevering.avlevering.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.consumer.sts.StsRestConsumer;
import no.nav.dokarkivavlevering.avlevering.domain.BrukerMedNavnedata;
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

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties.BEHANDLINGSNUMMER;

/**
 * https://navikt.github.io/pdl
 */
@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String HEADER_PDL_TEMA = "Tema";
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "Behandlingsnummer";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsConsumer;
	private final AvleveringProperties avleveringProperties;

	public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
							  StsRestConsumer stsConsumer,
							  AvleveringProperties avleveringProperties) {
		this.restTemplate = restTemplateBuilder
				.connectTimeout(Duration.ofSeconds(3))
				.readTimeout(Duration.ofSeconds(20))
				.build();
		this.stsConsumer = stsConsumer;
		this.avleveringProperties = avleveringProperties;
	}

	@Retryable(retryFor = HttpServerErrorException.class)
	public Map<String, BrukerMedNavnedata> hentPersonBolk(Set<String> aktoerIds, String tema) {
		if (aktoerIds.isEmpty()) {
			return emptyMap();
		}
		try {
			final UriComponents uri = UriComponentsBuilder.fromUriString(avleveringProperties.getPdlurl()).build();
			final String serviceuserToken = "Bearer " + stsConsumer.getStsToken().getAccess_token();
			final RequestEntity<PdlRequest> requestEntity = RequestEntity.post(uri.toUri())
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header(HttpHeaders.AUTHORIZATION, serviceuserToken)
					.header(HEADER_PDL_NAV_CONSUMER_TOKEN, serviceuserToken)
					.header(HEADER_PDL_TEMA, tema)
					.header(HEADER_PDL_BEHANDLINGSNUMMER, BEHANDLINGSNUMMER)
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

	private Map<String, BrukerMedNavnedata> createResponseAsMap(List<PdlHentPersonBolkResponse.PdlHentPersonBolk> hentPersonBolk) {
		return hentPersonBolk.stream().collect(Collectors.toMap(PdlHentPersonBolkResponse.PdlHentPersonBolk::getIdent,
				PdlHentPersonBolkResponse.PdlHentPersonBolk::toBrukerMedNavnedata));
	}

	private PdlRequest mapRequest(final Set<String> aktoerIds) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("identer", aktoerIds);
		return PdlRequest.builder()
				.query("""
						query hentIdenterBolk($identer: [ID!]!) {
						  hentPersonBolk(identer: $identer) {
							ident
							person {
							  folkeregisteridentifikator {
								identifikasjonsnummer
							  }
							  navn {
								fornavn
								mellomnavn
								etternavn
								folkeregistermetadata {
								  gyldighetstidspunkt
								  opphoerstidspunkt
								  sekvens
								}
								metadata {
								  opplysningsId
								  historisk
								}
							  }
							}
							code
						  }
						}
						""")
				.variables(variables)
				.build();
	}
}
