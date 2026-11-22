package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.ports.input.FilterUnsubscribedProductsUseCase;
import com.marfeel.exercise.application.ports.input.ProductCatalogUseCase;
import com.marfeel.exercise.application.ports.output.ProductCatalogClient;
import com.marfeel.exercise.application.ports.output.ProductMetricsClient;
import com.marfeel.exercise.domain.model.ProductCatalog;
import com.marfeel.exercise.domain.vo.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductCatalogService implements ProductCatalogUseCase {

	private final ProductCatalogClient catalogClient;
	private final FilterUnsubscribedProductsUseCase unsubscribedProducts;
	private final ProductMetricsClient metricsClient;

	@Override
	public Mono<ProductCatalog> allProducts(UserType userType) {
		return unsubscribedProducts.filter(userType, Flux.defer(catalogClient::allProducts))
				.flatMap(metricsClient::getMetrics)
				.collectList()
				.map(products -> new ProductCatalog(userType, products));
	}
}
