package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2ItemsResponse;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class S2ProductCatalogCaller {

	private final S2Client s2Client;

	public Flux<ProductRef> getProducts() {
		return s2Client.getItems()
			.flatMapIterable(S2ItemsResponse::items)
			.map(S2ItemsResponse.ItemEntry::item)
			.map(item -> new S2Product("S2", item.itemId()));
	}
}
