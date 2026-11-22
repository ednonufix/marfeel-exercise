package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.filter;

import com.marfeel.exercise.infrastructure.adapter.rest.output.token.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Slf4j
public record TokenFilterFunction(TokenProvider provider) implements ExchangeFilterFunction {

	@Override
	public @NonNull Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
		return provider.getToken()
				.flatMap(token -> {
					log.debug("Injecting Bearer token into request: {} {}", request.method(), request.url());
					var authenticatedRequest = ClientRequest.from(request)
							.headers(headers -> headers.setBearerAuth(token.token())).build();
					return next.exchange(authenticatedRequest);
				});
	}
}
