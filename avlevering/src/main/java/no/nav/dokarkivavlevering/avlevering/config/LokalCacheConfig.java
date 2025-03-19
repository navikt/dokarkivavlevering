package no.nav.dokarkivavlevering.avlevering.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class LokalCacheConfig {
	public static final String REST_STS_CACHE = "RESTSTS";

	@Bean
	@Primary
    CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Collections.singletonList(
				new CaffeineCache(REST_STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, TimeUnit.MINUTES)
						.maximumSize(1)
						.build())
		));
		return manager;
	}
}
