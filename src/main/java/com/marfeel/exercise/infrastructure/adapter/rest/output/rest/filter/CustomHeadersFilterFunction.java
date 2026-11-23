package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.filter;

import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

public record CustomHeadersFilterFunction(MultiValueMap<String, String> headersMap) implements ExchangeFilterFunction {
	@Override
	public @NonNull Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
		var authenticatedRequest = ClientRequest.from(request)
			.headers(headers -> headers.putAll(headersMap)).build();
		return next.exchange(authenticatedRequest);
	}
}
