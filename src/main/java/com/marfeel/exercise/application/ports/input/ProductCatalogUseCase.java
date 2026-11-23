package com.marfeel.exercise.application.ports.input;

import com.marfeel.exercise.domain.model.ProductCatalog;
import com.marfeel.exercise.domain.vo.UserType;

import reactor.core.publisher.Mono;

public interface ProductCatalogUseCase {

	Mono<ProductCatalog> allProducts(UserType userType);
}
