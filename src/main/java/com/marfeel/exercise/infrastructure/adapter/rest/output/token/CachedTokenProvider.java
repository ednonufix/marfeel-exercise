package com.marfeel.exercise.infrastructure.adapter.rest.output.token;

import java.time.Duration;

import reactor.core.publisher.Mono;

public final class CachedTokenProvider implements TokenProvider {

	private static final long REFRESH_MARGIN_SECONDS = 30;

	private final Mono<Token> cachedToken;

	public CachedTokenProvider(Mono<TokenResponse> tokenResponse) {
		this.cachedToken = tokenResponse
				.switchIfEmpty(Mono.error(() -> new IllegalStateException("Token endpoint returned an empty response")))
				.map(response -> new CachedToken(response.token(), extractTtl(response.expiresIn())))
				.cache(CachedToken::ttl, ignored -> Duration.ZERO, () -> Duration.ZERO)
				.cast(Token.class);
	}

	@Override
	public Mono<Token> getToken() {
		return cachedToken;
	}

	private static Duration extractTtl(int expiresIn) {
		var expiration = Duration.ofSeconds(expiresIn).minusSeconds(REFRESH_MARGIN_SECONDS);
		return expiration.isNegative() ? Duration.ZERO : expiration;
	}

}
