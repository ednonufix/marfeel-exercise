package com.marfeel.exercise.application.ports.input;

import java.util.List;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.SubscriptionListFilter;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.application.dto.UserSubscriptions;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserSubscriptionsUseCase {

	Mono<UserSubscriptions> getSubscriptions(SubscriptionListFilter subscriptionListFilter);

	Mono<SubscriptionWithDetail> getSubscription(UserType userType, long subscriptionId);

	Mono<List<SubscriptionWithDetail>> unsubscribe(UserType userType, long subscriptionId);

	Flux<Attempt> findAttempts(UserType userType, SubscriptionStatus status, String service);
}
