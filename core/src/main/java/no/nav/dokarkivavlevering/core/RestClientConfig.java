package no.nav.dokarkivavlevering.core;

import no.nav.dokarkivavlevering.core.consumer.nais.NaisTexasConsumer;
import no.nav.dokarkivavlevering.core.consumer.nais.NaisTexasRequestInterceptor;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder()
				.requestFactory(jdkClientHttpRequestFactory());
	}

	@Bean
	RestClient restClientTexas(RestClient.Builder restClientBuilder, NaisTexasConsumer naisTexasConsumer) {
		return restClientBuilder
				.requestFactory(jdkClientHttpRequestFactory())
				.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
				.build();
	}

	private static JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
		return ClientHttpRequestFactoryBuilder.jdk()
				.withHttpClientCustomizer(httpClient -> httpClient.connectTimeout(Duration.ofSeconds(5)))
				.withCustomizer(jdkClientHttpRequestFactory -> jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(30)))
				.build();
	}

}