package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import java.time.Duration;
import java.util.List;

import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1ProductsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S1ProductCatalogCallerTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private @Mock S1Client client;
	private S1ProductCatalogCaller caller;

	@BeforeEach
	void setUp() {
		caller = new S1ProductCatalogCaller(client);
	}

	@Test
	void mapsS1ProductEntriesAndKeepsDistinctSubcodes() {
		given(client.getProducts()).willReturn(Mono.just(new S1ProductsResponse(List.of(
				s1Entry("shared", "X1"), s1Entry("shared", "X2")))));

		caller.getProducts().as(StepVerifier::create)
				.expectNext(new S1Product("S1", "shared", "X1"), new S1Product("S1", "shared", "X2"))
				.expectComplete().verify(TIMEOUT);
	}

	private static S1ProductsResponse.ProductEntry s1Entry(String code, String subcode) {
		return new S1ProductsResponse.ProductEntry(new S1ProductsResponse.Product(code, subcode));
	}
}
