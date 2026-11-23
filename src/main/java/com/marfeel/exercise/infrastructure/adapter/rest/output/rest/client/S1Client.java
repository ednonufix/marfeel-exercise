package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1ProductsResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import reactor.core.publisher.Mono;

@HttpExchange
public interface S1Client {

	@GetExchange("/s1/products")
	Mono<S1ProductsResponse> getProducts();

	@PostExchange("/s1/subscribe")
	Mono<S1SubscribeResponse> subscribe(@RequestBody S1SubscribeRequest request);
}
