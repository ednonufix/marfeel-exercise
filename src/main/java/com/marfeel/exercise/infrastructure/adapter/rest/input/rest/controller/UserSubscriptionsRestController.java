package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.controller;

import com.marfeel.exercise.application.ports.input.ProductSubscriptionUseCase;
import com.marfeel.exercise.application.ports.input.UserSubscriptionsUseCase;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.mapper.ProductCatalogMapper;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.CriteriaActiveProductRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.ProductSubscriptionRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductSubscriptionResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.UserSubscriptionsResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui.UserSubscriptionsRestUI;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class UserSubscriptionsRestController implements UserSubscriptionsRestUI {

	private final ProductSubscriptionUseCase subscriptionUseCase;
	private final UserSubscriptionsUseCase userSubscriptionsUseCase;
	private final ProductCatalogMapper mapper;

	@Override
	public Mono<UserSubscriptionsResponse> getSubscriptions(UserType userId, CriteriaActiveProductRequest request) {
		return userSubscriptionsUseCase.getSubscriptions(mapper.toSubscriptionList(userId, request)).map(mapper::fromDomain);
	}

	@Override
	public Mono<UserSubscriptionsResponse.Active> getSubscription(UserType userId, long subscriptionId) {
		return userSubscriptionsUseCase.getSubscription(userId, subscriptionId)
				.map(mapper::fromDomain)
				.switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
	}

	@Override
	public Mono<List<UserSubscriptionsResponse.Active>> unsubscribe(UserType userId, long subscriptionId) {
		return userSubscriptionsUseCase.unsubscribe(userId, subscriptionId)
				.map(subscriptions -> subscriptions.stream().map(mapper::fromDomain).toList());
	}

	@Override
	public Mono<ProductSubscriptionResponse> subscribe(UserType userId, ProductSubscriptionRequest request) {
		var products = request.products().stream().map(mapper::toRef).toList();
		return subscriptionUseCase.subscribe(userId, Flux.fromIterable(products)).map(mapper::fromDomain);
	}

	@Override
	public Flux<UserSubscriptionsResponse.Attempt> attempts(UserType userId, SubscriptionStatus status, String service) {
		return userSubscriptionsUseCase.findAttempts(userId, status, service).map(mapper::fromDomain);
	}
}
