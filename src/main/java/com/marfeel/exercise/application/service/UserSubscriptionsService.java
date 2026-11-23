package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.application.dto.SubscriptionListFilter;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.application.dto.UserSubscriptions;
import com.marfeel.exercise.application.mapper.SubscriptionMapper;
import com.marfeel.exercise.application.ports.input.UserSubscriptionsUseCase;
import com.marfeel.exercise.application.ports.output.ProductMetricsClient;
import com.marfeel.exercise.application.ports.output.UserSubscriptionsRepository;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.stereotype.Service;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserSubscriptionsService implements UserSubscriptionsUseCase {

	private final ProductMetricsClient metricsClient;
	private final UserSubscriptionsRepository repository;
	private final SubscriptionMapper mapper;

	@Override
	public Mono<UserSubscriptions> getSubscriptions(SubscriptionListFilter subscriptionListFilter) {
		return Mono.zip(
			activeSubscription(subscriptionListFilter).collectList(),
			repository.findAttempts(subscriptionListFilter.userType(), null, null).collectList(),
			UserSubscriptions::new);
	}

	@Override
	public Mono<SubscriptionWithDetail> getSubscription(UserType userType, long subscriptionId) {
		return repository.findActiveSubscription(userType, subscriptionId).flatMap(this::withMetrics);
	}

	@Override
	public Mono<List<SubscriptionWithDetail>> unsubscribe(UserType userType, long subscriptionId) {
		return repository.deleteActiveSubscription(userType, subscriptionId)
			.then(activeSubscription(new SubscriptionListFilter(userType, null)).collectList());
	}

	@Override
	public Flux<Attempt> findAttempts(UserType userType, SubscriptionStatus status, String service) {
		return repository.findAttempts(userType, status, service);
	}

	private Flux<SubscriptionWithDetail> activeSubscription(SubscriptionListFilter filter) {
		return repository.findActiveSubscriptions(filter.userType(), filter.criteria()).flatMap(this::withMetrics);
	}

	private Mono<SubscriptionWithDetail> withMetrics(Subscription subscription) {
		return metricsClient.getMetrics(subscription.ref()).map(detail -> mapper.toDetail(subscription, detail));
	}
}
