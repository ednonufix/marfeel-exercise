package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client;

import com.marfeel.exercise.infrastructure.adapter.rest.output.token.TokenResponse;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface A1Client {

	@PostExchange("/a1/token")
	Mono<TokenResponse> fetchToken();
}
