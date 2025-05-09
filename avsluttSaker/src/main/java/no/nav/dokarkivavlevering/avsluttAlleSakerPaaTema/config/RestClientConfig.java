package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("avsluttSaker")
public class RestClientConfig {
	@Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}
}
