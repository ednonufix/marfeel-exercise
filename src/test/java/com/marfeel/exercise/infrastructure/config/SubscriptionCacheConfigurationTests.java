package com.marfeel.exercise.infrastructure.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionCacheConfigurationTests {

	@Test
	void exposesOnlyTheInProgressCacheWithTwoMinuteExpirationAfterWrite() {
		var manager = new SubscriptionCacheConfiguration().subscriptionCacheManager();

		assertThat(manager.getCacheNames()).containsExactly("subscriptionsInProgress");
		assertThat(manager.getCache("unknown")).isNull();
		var cache = (CaffeineCache) manager.getCache("subscriptionsInProgress");
		assertThat(cache).isNotNull();
		assertThat(cache.getNativeCache().policy().expireAfterWrite().orElseThrow().getExpiresAfter())
				.isEqualTo(Duration.ofMinutes(2));
		assertThat(cache.getNativeCache().policy().expireAfterAccess()).isEmpty();
	}
}
