package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.application.ports.output.ProductCatalogClient;
import com.marfeel.exercise.domain.model.ProductRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
public class ProductCatalogCaller implements ProductCatalogClient {

	private final S1ProductCatalogCaller s1Caller;
	private final S2ProductCatalogCaller s2Caller;

	@Override
	public Flux<ProductRef> allProducts() {
		return Flux.merge(s1Caller.getProducts(), s2Caller.getProducts());
	}
}
