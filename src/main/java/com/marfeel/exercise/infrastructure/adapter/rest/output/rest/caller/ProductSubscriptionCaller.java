package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionClient;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.ProviderSubscriptionRequestMapper;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.ProviderSubscriptionResponseMapper;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ProductSubscriptionCaller implements ProductSubscriptionClient {

	private final S1Client s1Client;
	private final S2Client s2Client;
	private final ProviderSubscriptionRequestMapper requestMapper;
	private final ProviderSubscriptionResponseMapper responseMapper;

	@Override
	public Mono<ProviderSubscriptionResult> subscribe(UserType userType, ProductRef product) {
		return switch (product) {
			case S1Product s1 -> s1Client.subscribe(requestMapper.toS1Request(userType.name(), s1)).map(responseMapper::fromS1);
			case S2Product s2 -> s2Client.subscribe(requestMapper.toS2Request(userType.name(), s2)).map(responseMapper::fromS2);
		};
	}
}
