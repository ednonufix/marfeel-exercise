package com.marfeel.exercise.infrastructure.adapter.cache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineSubscriptionProcessingFlowAdapterTests {

	@Test
	void expiredOwnerCannotReleaseTheNextReservation() {
		var ttl = Duration.ofSeconds(2);
		var clock = new AtomicLong();
		var manager = new CaffeineCacheManager("subscriptionsInProgress");
		manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(ttl).ticker(clock::get));
		var cache = new CaffeineSubscriptionProcessingFlowAdapter(manager);
		var product = new S2Product("S2", "S2-100");
		var first = cache.tryAcquire(UserType.U1, product).orElseThrow();
		clock.set(ttl.toNanos() - 1);
		assertThat(cache.tryAcquire(UserType.U1, product)).isEmpty();
		clock.incrementAndGet();
		var second = cache.tryAcquire(UserType.U1, product).orElseThrow();
		first.release();
		assertThat(cache.tryAcquire(UserType.U1, product)).isEmpty();
		second.release();
		assertThat(cache.tryAcquire(UserType.U1, product)).isPresent();
	}
}
