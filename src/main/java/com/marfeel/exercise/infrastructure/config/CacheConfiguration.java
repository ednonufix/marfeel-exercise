package com.marfeel.exercise.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;

@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

	@Bean
	public CaffeineCacheManager subscriptionCacheManager() {
		var manager = new CaffeineCacheManager();
		manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(2)));
		manager.setCacheNames(List.of("subscriptionsInProgress"));
		manager.registerCustomCache("s1Products", Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(2))
			.buildAsync());
		return manager;
	}
}
