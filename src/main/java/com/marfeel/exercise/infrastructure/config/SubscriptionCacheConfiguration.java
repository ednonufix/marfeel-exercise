package com.marfeel.exercise.infrastructure.config;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SubscriptionCacheConfiguration {

	@Bean
	public CaffeineCacheManager subscriptionCacheManager() {
		var manager = new CaffeineCacheManager();
		manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(2)));
		manager.setCacheNames(List.of("subscriptionsInProgress"));
		return manager;
	}
}
