package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2ItemsResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface S2Client {

	@GetExchange("/s2/items")
	Mono<S2ItemsResponse> getItems();

	@PostExchange("/s2/subscribe")
	Mono<S2SubscribeResponse> subscribe(@RequestBody S2SubscribeRequest request);
}
