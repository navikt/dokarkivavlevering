package no.nav.dokarkivavlevering.avlevering.consumer.ereg;

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.consumer.sts.StsRestConsumer;
import org.apache.http.HttpHeaders;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
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
import static net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils.trimToEmpty;

@Component
public class EregConsumer {
	private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;
	private final String eregUrl;

	public EregConsumer(RestTemplateBuilder restTemplateBuilder,
						StsRestConsumer stsRestConsumer,
						AvleveringProperties avleveringProperties) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.build();
		this.stsRestConsumer = stsRestConsumer;
		this.eregUrl = avleveringProperties.getEregurl();
	}

	@Retryable(include = HttpServerErrorException.class)
	public String hentNavn(final String orgnr) {
		if (isValidOrgnrFormat(orgnr)) {
			try {
				final URI uri = UriComponentsBuilder.fromUriString(eregUrl).pathSegment(orgnr + "/noekkelinfo").build().toUri();
				final String serviceuserToken = "Bearer " + stsRestConsumer.getStsToken().getAccess_token();

				final RequestEntity requestEntity = RequestEntity.get(uri)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.header(HttpHeaders.AUTHORIZATION, serviceuserToken)
						.header(NAV_CONSUMER_TOKEN, serviceuserToken)
						.build();

				ResponseEntity<EregHentNoekkelInfoResponse> response =
						requireNonNull(restTemplate.exchange(requestEntity, EregHentNoekkelInfoResponse.class));

				return Optional.ofNullable(response.getBody())
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
