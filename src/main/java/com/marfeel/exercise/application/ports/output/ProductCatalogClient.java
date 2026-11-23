package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.domain.model.ProductRef;

import reactor.core.publisher.Flux;

public interface ProductCatalogClient {

	Flux<ProductRef> allProducts();

}
