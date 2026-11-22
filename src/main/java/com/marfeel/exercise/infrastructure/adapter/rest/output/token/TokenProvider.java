package com.marfeel.exercise.infrastructure.adapter.rest.output.token;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface TokenProvider {

	Mono<Token> getToken();
}
