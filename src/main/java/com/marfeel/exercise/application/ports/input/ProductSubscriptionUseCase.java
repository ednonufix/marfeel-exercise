package com.marfeel.exercise.application.ports.input;

import com.marfeel.exercise.application.dto.SubscriptionBatchResult;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductSubscriptionUseCase {

	Mono<SubscriptionBatchResult> subscribe(UserType userType, Flux<ProductRef> products);
}
