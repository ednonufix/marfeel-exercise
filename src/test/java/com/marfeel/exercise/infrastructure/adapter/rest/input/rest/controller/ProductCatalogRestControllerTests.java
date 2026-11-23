package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.controller;

import com.marfeel.exercise.application.ports.input.ProductCatalogUseCase;
import com.marfeel.exercise.domain.model.ProductCatalog;
import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.mapper.ProductCatalogMapperImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@WebFluxTest(ProductCatalogRestController.class)
@Import({ProductCatalogMapperImpl.class, ControllerSecurityTestConfiguration.class})
class ProductCatalogRestControllerTests {

	private @Autowired WebTestClient client;

	private @MockitoBean ProductCatalogUseCase catalog;

	@Test
	void returnsMetricsAndDiscriminatedProductReferences() {
		assertDoesNotThrow(() -> {
			var s1 = new ProductDetail(new S1Product("S1", "A", "X1"), 12, "ES01");
			var s2 = new ProductDetail(new S2Product("S2", "item-7"), 3, "ES02");
			given(catalog.allProducts(UserType.U1)).willReturn(Mono.just(new ProductCatalog(UserType.U1, List.of(s1, s2))));

			client.get().uri("/users/u1/catalog").header("API-Version", "v1")
				.headers(headers -> headers.setBasicAuth("U1", "U1"))
				.exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.userId").isEqualTo("U1")
				.jsonPath("$.products.length()").isEqualTo(2)
				.jsonPath("$.products[0].ref.service").isEqualTo("S1")
				.jsonPath("$.products[0].ref.code").isEqualTo("A")
				.jsonPath("$.products[0].ref.subcode").isEqualTo("X1")
				.jsonPath("$.products[0].subscribers").isEqualTo(12)
				.jsonPath("$.products[0].iban").isEqualTo("ES01")
				.jsonPath("$.products[1].ref.service").isEqualTo("S2")
				.jsonPath("$.products[1].ref.itemId").isEqualTo("item-7");
		});
	}

	@Test
	void rejectsRequestsWithoutCredentials() {
		client.get().uri("/users/U1/catalog").header("API-Version", "v1")
			.exchange().expectStatus().isUnauthorized();

		verifyNoInteractions(catalog);
	}

	@Test
	void forbidsReadingAnotherUsersCatalog() {
		client.get().uri("/users/U2/catalog").header("API-Version", "v1")
			.headers(headers -> headers.setBasicAuth("U1", "U1"))
			.exchange().expectStatus().isForbidden();

		verifyNoInteractions(catalog);
	}
}
