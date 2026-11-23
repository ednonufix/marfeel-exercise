package com.marfeel.exercise.infrastructure.adapter.rest.output.token;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated("Reactor virtual time replaces shared schedulers")
class CachedTokenProviderTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	@Test
	void refreshesTokensThirtySecondsBeforeExpiration() {
		var calls = new AtomicInteger();

		StepVerifier.withVirtualTime(() -> {
					var provider = new CachedTokenProvider(Mono.fromSupplier(
							() -> new TokenResponse("token-" + calls.incrementAndGet(), 120)));
					return provider.getToken().concatWith(provider.getToken().delaySubscription(Duration.ofSeconds(89)))
							.concatWith(provider.getToken().delaySubscription(Duration.ofSeconds(2)))
							.map(Token::token);
				}).expectNext("token-1").thenAwait(Duration.ofSeconds(89)).expectNext("token-1")
				.thenAwait(Duration.ofSeconds(2)).expectNext("token-2").expectComplete().verify(TIMEOUT);

		assertThat(calls.get()).isEqualTo(2);
	}

	@Test
	void retriesAfterAnEmptyResponseAndDoesNotCacheShortLivedTokens() {
		var calls = new AtomicInteger();
		var provider = new CachedTokenProvider(Mono.defer(() -> {
			int call = calls.incrementAndGet();
			return call == 1 ? Mono.empty() : Mono.just(new TokenResponse("token-" + call, 10));
		}));

		StepVerifier.create(provider.getToken()).expectErrorMessage("Token endpoint returned an empty response").verify(TIMEOUT);
		StepVerifier.create(provider.getToken().concatWith(provider.getToken()).map(Token::token))
				.expectNext("token-2", "token-3").expectComplete().verify(TIMEOUT);
	}
}
