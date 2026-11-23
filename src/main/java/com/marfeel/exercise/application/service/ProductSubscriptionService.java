package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.dto.SubscriptionBatchResult;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.mapper.SaveSubscriptionCommandMapper;
import com.marfeel.exercise.application.ports.input.ProductSubscriptionUseCase;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.application.ports.output.SubscriptionProcessingFlow;
import com.marfeel.exercise.application.subscription.SubscriptionAttempt;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductSubscriptionService implements ProductSubscriptionUseCase {

	private final SubscriptionProcessingFlow processing;
	private final ProductSubscriptionRepository subscriptionRepository;
	private final SaveSubscriptionCommandMapper subscriptionCommandMapper;
	private final SubscriptionAttempt subscriptionAttempt;

	@Override
	public Mono<SubscriptionBatchResult> subscribe(UserType userType, Flux<ProductRef> products) {
		return products.distinct()
			.flatMap(product -> reserveAndSubscribe(userType, product))
			.collectList()
			.map(SubscriptionBatchResult::new);
	}

	private Mono<Result> reserveAndSubscribe(UserType userType, ProductRef product) {
		return Mono.defer(() -> {
			var reservation = processing.tryAcquire(userType, product);
			return reservation.map(value -> subscribeIfInactive(userType, product).doOnTerminate(value::release))
				.orElseGet(() -> Mono.just(subscriptionCommandMapper.existing(product, SubscriptionStatus.IN_PROGRESS)));
		});
	}

	private Mono<Result> subscribeIfInactive(UserType userType, ProductRef product) {
		return Mono.defer(() -> subscriptionRepository.isActive(userType, product))
			.flatMap(active -> {
				if (active) {
					return Mono.just(subscriptionCommandMapper.existing(product, SubscriptionStatus.ALREADY_SUBSCRIBED));
				}
				return subscriptionAttempt.execute(userType, product);
			});
	}
}
