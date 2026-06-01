package no.nav.dokarkivavlevering.core.consumer.nais;

import com.fasterxml.jackson.annotation.JsonProperty;

record NaisTexasToken(
		@JsonProperty("access_token") String accessToken
) {
}