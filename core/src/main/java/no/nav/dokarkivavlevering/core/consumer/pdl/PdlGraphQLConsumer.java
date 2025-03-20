package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static no.nav.dokarkivavlevering.core.DokarkivavleveringProperties.BEHANDLINGSNUMMER;
import static no.nav.dokarkivavlevering.core.azure.AzureProperties.CLIENT_REGISTRATION_PDL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * https://navikt.github.io/pdl
 */
@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String HEADER_PDL_NAV_CALL_ID = "Nav-Call-Id";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel_vha_tema
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";

	private final DokarkivavleveringProperties dokarkivavleveringProperties;
	private final WebClient webClient;

	public PdlGraphQLConsumer(DokarkivavleveringProperties dokarkivAvleveringProperties,
							  WebClient webClient) {
		this.dokarkivavleveringProperties = dokarkivAvleveringProperties;
		this.webClient = webClient.mutate()
				.defaultHeaders(headers -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(HEADER_PDL_BEHANDLINGSNUMMER, BEHANDLINGSNUMMER);
					//TODO: Fix callid
					headers.set(HEADER_PDL_NAV_CALL_ID, "dette_er_en_dårlig_call_id");
					//TODO: ER denne deprecated?
					//headers.set("TEMA", tema)
				})
				.build();
	}

	@Retryable(retryFor = HttpServerErrorException.class)
	public List<PdlHentPersonBolkResponse.PdlHentPersonBolk> hentPersonBolk(Set<String> aktoerIds) {
		PdlHentPersonBolkResponse pdlResponse = webClient.post()
				.uri(dokarkivavleveringProperties.getEndpoints().getPdl().getUrl())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapRequest(aktoerIds))
				.retrieve()
				.bodyToMono(PdlHentPersonBolkResponse.class)
				.doOnError(handleErrorPdl())
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentPersonBolk();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke person i Persondataløsningen (PDL).");
			}
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
		}
	}

	private Consumer<Throwable> handleErrorPdl() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", error);
			}
		};
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