package no.nav.dokarkivavlevering.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		var httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();

		var requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(30));

		return RestClient.builder()
				.requestFactory(requestFactory);
	}

}