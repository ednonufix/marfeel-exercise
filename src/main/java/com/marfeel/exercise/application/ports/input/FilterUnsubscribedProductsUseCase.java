package com.marfeel.exercise.application.ports.input;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import reactor.core.publisher.Flux;

public interface FilterUnsubscribedProductsUseCase {

	Flux<ProductRef> filter(UserType userType, Flux<ProductRef> products);
}
