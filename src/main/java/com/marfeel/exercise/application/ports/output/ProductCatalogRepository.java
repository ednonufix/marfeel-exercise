package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import reactor.core.publisher.Flux;

public interface ProductCatalogRepository {

	Flux<ProductRef> findActiveProductRefs(UserType userType);

}
