package com.marfeel.exercise.infrastructure.config;

import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigurationTests {

	private CaffeineCacheManager manager;

	@BeforeEach
	void setUp() {
		manager = new CacheConfiguration().subscriptionCacheManager();
	}

	@Test
	void registersOnlyTheConfiguredCaches() {
		assertThat(manager.getCacheNames()).containsExactlyInAnyOrder("subscriptionsInProgress", "s1Products");
		assertThat(manager.getCache("unknown")).isNull();
	}

	@Test
	void expiresInProgressReservationsTwoMinutesAfterWrite() {
		var cache = (CaffeineCache) manager.getCache("subscriptionsInProgress");
		assertThat(cache).isNotNull();
		var policy = cache.getNativeCache().policy();

		assertThat(policy.expireAfterWrite().orElseThrow().getExpiresAfter()).isEqualTo(Duration.ofMinutes(2));
		assertThat(policy.expireAfterAccess()).isEmpty();
	}

	@Test
	void cachesS1ProductsAsynchronouslyForTwoHoursAfterWrite() {
		var cache = (CaffeineCache) manager.getCache("s1Products");
		assertThat(cache).isNotNull();
		var policy = cache.getAsyncCache().synchronous().policy();

		assertThat(policy.expireAfterWrite().orElseThrow().getExpiresAfter()).isEqualTo(Duration.ofHours(2));
		assertThat(policy.expireAfterAccess()).isEmpty();
	}
}
