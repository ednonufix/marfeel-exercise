package com.marfeel.exercise.infrastructure.adapter.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import com.marfeel.exercise.application.ports.output.SubscriptionProcessingFlow;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Component;

@Component
public class CaffeineSubscriptionProcessingFlowAdapter implements SubscriptionProcessingFlow {

	private final ConcurrentMap<Object, Object> entries;

	public CaffeineSubscriptionProcessingFlowAdapter(@Qualifier("subscriptionCacheManager") CaffeineCacheManager manager) {
		var cache = (CaffeineCache) Objects.requireNonNull(manager.getCache("subscriptionsInProgress"));
		this.entries = cache.getNativeCache().asMap();
	}

	@Override
	public Optional<Reservation> tryAcquire(UserType user, ProductRef product) {
		var key = new Key(user, product);
		var owner = new Object();
		if (entries.putIfAbsent(key, owner) != null) {
			return Optional.empty();
		}
		return Optional.of(() -> entries.remove(key, owner));
	}
}
