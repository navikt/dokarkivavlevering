package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.core.DokarkivavleveringProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class EregConsumer {

	private final RestTemplate restTemplate;
	private final String eregUrl;

	public EregConsumer(RestTemplateBuilder restTemplateBuilder,
						DokarkivavleveringProperties avleveringProperties) {
		this.restTemplate = restTemplateBuilder
				.connectTimeout(Duration.ofSeconds(3))
				.readTimeout(Duration.ofSeconds(20))
				.build();
		this.eregUrl = avleveringProperties.getEregurl();
	}

	@Retryable(retryFor = HttpServerErrorException.class)
	public String hentNavn(final String orgnr) {
		if (isValidOrgnrFormat(orgnr)) {
			try {
				final URI uri = UriComponentsBuilder.fromUriString(eregUrl).pathSegment(orgnr + "/noekkelinfo").build().toUri();

				final RequestEntity<Void> requestEntity = RequestEntity.get(uri)
						.accept(APPLICATION_JSON)
						.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.build();

				log.debug("Henter organisasjonavn for orgnr={}", orgnr);
				ResponseEntity<EregHentNoekkelInfoResponse> response =
						requireNonNull(restTemplate.exchange(requestEntity, EregHentNoekkelInfoResponse.class));
				log.debug("Ferdig hentet organisasjonavn for orgnr={}", orgnr);
				return Optional.of(response.getBody())
						.map(EregHentNoekkelInfoResponse::getNavn)
						.map(this::getFullName)
						.orElse("Ukjent organisasjonsnavn");

			} catch (HttpClientErrorException.NotFound e) {
				return "Ukjent organisasjonsnummer";
			} catch (HttpClientErrorException e) {
				throw new EregFunctionalException(format("Funsjonell feil ved kall mot ereg:hentNoekkelinfo for organisasjonsnummer=%s. feilmelding=%s",
						orgnr, e.getMessage()), e);
			}
		}
		return "Ugyldig organisasjonsnummer";
	}

	private String getFullName(EregHentNoekkelInfoResponse.Navn navn) {
		return trimToEmpty(format("%s %s %s %s %s", trimToEmpty(navn.getNavnelinje1()), trimToEmpty(navn.getNavnelinje2()),
				trimToEmpty(navn.getNavnelinje3()), trimToEmpty(navn.getNavnelinje4()), trimToEmpty(navn.getNavnelinje5())));
	}

	private static boolean isValidOrgnrFormat(String orgnr) {
		return StringUtils.isNotBlank(orgnr) && orgnr.matches("^\\d{9}$");
	}
}
