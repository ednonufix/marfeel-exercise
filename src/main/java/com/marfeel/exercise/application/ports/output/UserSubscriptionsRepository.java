package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserSubscriptionsRepository {

	Flux<Subscription> findActiveSubscriptions(UserType userType, CriteriaFilter criteria);

	Mono<Subscription> findActiveSubscription(UserType userType, long subscriptionId);

	Mono<Void> deleteActiveSubscription(UserType userType, long subscriptionId);

	Flux<Attempt> findAttempts(UserType userType, SubscriptionStatus status, String service);
}
