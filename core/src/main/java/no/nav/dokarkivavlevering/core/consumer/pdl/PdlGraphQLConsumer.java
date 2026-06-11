package no.nav.dokarkivavlevering.core.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import no.nav.dokarkivavlevering.core.consumer.pdl.HentIdenterBolkResponse.HentIdenterBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.PdlHentPersonBolkResponse.PdlHentPersonBolk;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlFunctionalException;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PdlTechnicalException;
import no.nav.dokarkivavlevering.core.consumer.pdl.exception.PersonIkkeFunnetException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static no.nav.dokarkivavlevering.core.consumer.nais.NaisTexasRequestInterceptor.TARGET_SCOPE;

@Slf4j
@Component
public class PdlGraphQLConsumer {

	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B524";
	private static final String HEADER_BEHANDLINGSNUMMER = "behandlingsnummer";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestClient restClient;
	private final String targetScope;

	public PdlGraphQLConsumer(RestClient restClientTexas,
	                          DokarkivavleveringProperties dokarkivAvleveringProperties) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokarkivAvleveringProperties.getEndpoints().getPdl().getUrl())
				.defaultHeader(HEADER_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER)
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokarkivAvleveringProperties.getEndpoints().getPdl().getScope();
	}

	@Retryable(includes = PdlTechnicalException.class)
	public List<PdlHentPersonBolk> hentPersonBolk(Set<String> aktoerIds) {
		PdlHentPersonBolkResponse pdlResponse = restClient.post()
				.attribute(TARGET_SCOPE, targetScope)
				.body(mapHentPersonBolk(aktoerIds))
				.retrieve()
				.body(PdlHentPersonBolkResponse.class);

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentPersonBolk();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke person i Persondataløsningen (PDL).");
			}
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
		}
	}

	@Retryable(includes = PdlTechnicalException.class)
	public List<HentIdenterBolk> hentGjeldendeAktoerIder(Set<String> aktoerIds) {
		HentIdenterBolkResponse pdlResponse = restClient.post()
				.attribute(TARGET_SCOPE, targetScope)
				.body(mapHentGjeldendeAktoerIdForBolk(aktoerIds))
				.retrieve()
				.body(HentIdenterBolkResponse.class);

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			log.info("hentGjeldendeAktoerIder har hentet svar fra PDL OK");
			return pdlResponse.getData().getHentIdenterBolk();
		} else {
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
		}
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		if (response.getStatusCode().is4xxClientError()) {
			throw new PdlFunctionalException(
					format("Kall mot pdl feilet funksjonelt med statuskode=%s Feilmelding=%s",
							response.getStatusCode(), body));
		} else {
			throw new PdlTechnicalException(
					format("Kall mot pdl feilet teknisk med statuskode=%s Feilmelding=%s",
							response.getStatusCode(), body), null);
		}
	}

	private PdlRequest mapHentPersonBolk(final Set<String> aktoerIds) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("identer", aktoerIds);
		return PdlRequest.builder()
				.query("""
						query hentPersonBolk($identer: [ID!]!) {
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