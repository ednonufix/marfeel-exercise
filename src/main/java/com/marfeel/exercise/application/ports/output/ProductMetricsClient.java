package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.ProductRef;
import reactor.core.publisher.Mono;

public interface ProductMetricsClient {

	Mono<ProductDetail> getMetrics(ProductRef ref);

}
