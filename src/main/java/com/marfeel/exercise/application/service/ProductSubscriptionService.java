package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.mapper.SaveSubscriptionCommandMapper;
import com.marfeel.exercise.application.ports.input.FilterUnsubscribedProductsUseCase;
import com.marfeel.exercise.application.ports.input.ProductSubscriptionUseCase;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionClient;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

@RequiredArgsConstructor
@Service
public class ProductSubscriptionService implements ProductSubscriptionUseCase {

	private final FilterUnsubscribedProductsUseCase unsubscribedProducts;
	private final ProductSubscriptionClient subscriptionClient;
	private final ProductSubscriptionRepository subscriptionRepository;
	private final SaveSubscriptionCommandMapper subscriptionCommandMapper;

	@Override
	public Mono<SubscriptionBatchResult> subscribe(UserType userType, Flux<ProductRef> products) {
		return unsubscribedProducts.filter(userType, products).collect(Collectors.toSet())
				.flatMapMany(available -> products.distinct()
						.concatMap(product -> available.contains(product)
						                      ? subscribe(userType, product)
						                      : Mono.fromSupplier(() -> subscriptionCommandMapper.toAlreadySubscribed(product))))
				.collectList()
				.map(SubscriptionBatchResult::new);
	}

	private Mono<Result> subscribe(UserType userType, ProductRef product) {
		return Mono.defer(() -> subscriptionClient.subscribe(userType, product)).materialize()
				.map(outcome -> toCommand(userType, product, outcome))
				.flatMap(this::save);
	}

	private SaveSubscriptionCommand toCommand(UserType userType, ProductRef product,
			Signal<ProviderSubscriptionResult> outcome) {
		var result = Optional.ofNullable(outcome.get());
		var error = outcome.getThrowable();
		return result.map(ProviderSubscriptionResult::subscriptionId)
				.map(subscriptionId -> subscriptionCommandMapper.toSuccess(userType, product, subscriptionId))
				.orElseGet(() -> subscriptionCommandMapper.toFailure(userType, product,
						result.map(ProviderSubscriptionResult::message)
								.orElseGet(() -> error != null ? error.getMessage() : "Not provided error message")));
	}

	private @NonNull Mono<Result> save(SaveSubscriptionCommand command) {
		return subscriptionRepository.save(command).thenReturn(subscriptionCommandMapper.toResult(command));
	}

}
