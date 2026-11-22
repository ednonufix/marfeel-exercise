package com.marfeel.exercise.application.service;

import java.time.Duration;
import java.util.ArrayList;

import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogServiceTests {

	@Test
	void requestsMetricsForAvailableProductsBeforeTheCatalogCompletes() {
		var subscribedS1 = new S1Product("S1", "S1-A", "X1");
		var subscribedS2 = new S2Product("S2", "S2-100");
		var availableS1 = new S1Product("S1", "S1-A", "X2");
		var availableS2 = new S2Product("S2", "S2-200");
		var catalogRefs = TestPublisher.<ProductRef>create();
		var metricRequests = new ArrayList<ProductRef>();
		var service = new ProductCatalogService(
				catalogRefs::flux,
				new FilterUnsubscribedProductsService(user -> {
					assertThat(user).isEqualTo(UserType.U1);
					return Flux.just(subscribedS1, subscribedS2);
				}),
				ref -> {
					metricRequests.add(ref);
					return Mono.just(new ProductDetail(ref, 12, "ES01"));
				});

		service.allProducts(UserType.U1).as(StepVerifier::create)
				.then(() -> catalogRefs.next(subscribedS1, subscribedS2, availableS1, availableS2))
				.then(() -> assertThat(metricRequests).containsExactly(availableS1, availableS2))
				.then(catalogRefs::complete)
				.assertNext(catalog -> {
					assertThat(catalog.userId()).isEqualTo(UserType.U1);
					assertThat(catalog.products()).extracting(ProductDetail::ref)
							.containsExactlyInAnyOrder(availableS1, availableS2);
				})
				.expectComplete()
				.verify(Duration.ofSeconds(5));
	}
}
