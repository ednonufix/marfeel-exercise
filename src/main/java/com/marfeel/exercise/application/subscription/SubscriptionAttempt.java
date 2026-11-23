package com.marfeel.exercise.application.subscription;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.mapper.SaveSubscriptionCommandMapper;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionClient;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.stereotype.Component;

import java.util.Optional;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SubscriptionAttempt {

	private final ProductSubscriptionClient subscriptionClient;
	private final ProductSubscriptionRepository subscriptionRepository;
	private final SaveSubscriptionCommandMapper subscriptionCommandMapper;

	public Mono<Result> execute(UserType userType, ProductRef product) {
		return Mono.defer(() -> subscriptionClient.subscribe(userType, product))
			.materialize()
			.map(outcome -> toSaveCommand(userType, product, outcome))
			.flatMap(this::save);
	}

	private SaveSubscriptionCommand toSaveCommand(UserType userType, ProductRef product,
		Signal<ProviderSubscriptionResult> outcome) {
		var providerResult = Optional.ofNullable(outcome.get());
		var error = outcome.getThrowable();
		return providerResult.map(ProviderSubscriptionResult::subscriptionId)
			.map(subscriptionId -> subscriptionCommandMapper.toSuccess(userType, product, subscriptionId))
			.orElseGet(() -> subscriptionCommandMapper.toFailure(userType, product,
				providerResult.map(ProviderSubscriptionResult::message)
					.orElseGet(() -> error != null ? error.getMessage() : "Not provided error message")));
	}

	private Mono<Result> save(SaveSubscriptionCommand command) {
		return subscriptionRepository.save(command).thenReturn(subscriptionCommandMapper.toResult(command));
	}
}
