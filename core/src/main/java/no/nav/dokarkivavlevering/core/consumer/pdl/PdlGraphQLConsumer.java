package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlTechnicalException;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PersonIkkeFunnetException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static no.nav.dokarkivavlevering.core.azure.AzureProperties.CLIENT_REGISTRATION_PDL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * https://navikt.github.io/pdl
 */
@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B524";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String HEADER_BEHANDLINGSNUMMER = "behandlingsnummer";

	private final WebClient webClient;
	private final DokarkivavleveringProperties dokarkivavleveringProperties;

	public PdlGraphQLConsumer(WebClient webClient,
							  DokarkivavleveringProperties dokarkivAvleveringProperties) {
		this.dokarkivavleveringProperties = dokarkivAvleveringProperties;
		this.webClient = webClient.mutate()
				.defaultHeaders(headers -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(HEADER_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
					headers.set(HEADER_NAV_CALL_ID, UUID.randomUUID().toString());
				})
				.build();
	}

	@Retryable(retryFor = PdlTechnicalException.class)
	public List<PdlHentPersonBolkResponse.PdlHentPersonBolk> hentPersonBolk(Set<String> aktoerIds) {
		PdlHentPersonBolkResponse pdlResponse = webClient.post()
				.uri(dokarkivavleveringProperties.getEndpoints().getPdl().getUrl())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapHentPersonBolk(aktoerIds))
				.retrieve()
				.bodyToMono(PdlHentPersonBolkResponse.class)
				.onErrorMap(this::mapError)
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

	@Retryable(retryFor = PdlTechnicalException.class)
	public List<HentIdenterBolk> hentGjeldendeAktoerIder(Set<String> aktoerIds) {
		HentIdenterBolkResponse pdlResponse = webClient.post()
				.uri(dokarkivavleveringProperties.getEndpoints().getPdl().getUrl())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapHentGjeldendeAktoerIdForBolk(aktoerIds))
				.retrieve()
				.bodyToMono(HentIdenterBolkResponse.class)
				.onErrorMap(this::mapError)
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			log.info("hentGjeldendeAktoerIder har hentet svar fra PDL OK");
			return pdlResponse.getData().getHentIdenterBolk();
		} else {
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
		}
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new PdlFunctionalException(
					format("Kall mot pdl feilet funksjonelt med statuskode=%s Feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			return new PdlTechnicalException(error.getMessage(), error);
		}
	}

	private PdlRequest mapHentPersonBolk(final Set<String> aktoerIds) {
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

	private PdlRequest mapHentGjeldendeAktoerIdForBolk(final Set<String> aktoerIds) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("identer", aktoerIds);
		return PdlRequest.builder()
				.query("""
						query hentIdenterBolk($identer: [ID!]!) {
						   hentIdenterBolk(identer: $identer, grupper: [AKTORID], historikk: false) {
						         ident,
						         identer {
						             ident
						         },
						         code
						     }
						 }
						""")
				.variables(variables)
				.build();
	}
}