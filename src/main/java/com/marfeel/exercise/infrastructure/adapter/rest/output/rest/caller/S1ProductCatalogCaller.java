package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1ProductsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
public class S1ProductCatalogCaller {

	private final S1Client s1Client;

	public Flux<ProductRef> getProducts() {
		return s1Client.getProducts()
				.flatMapIterable(S1ProductsResponse::products)
				.map(S1ProductsResponse.ProductEntry::product)
				.map(product -> new S1Product("S1", product.code(), product.subcode()));
	}
}
