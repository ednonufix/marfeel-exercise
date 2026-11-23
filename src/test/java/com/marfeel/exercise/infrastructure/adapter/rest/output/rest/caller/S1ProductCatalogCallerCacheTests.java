package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1ProductsResponse;
import com.marfeel.exercise.infrastructure.config.CacheConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringJUnitConfig({CacheConfiguration.class, S1ProductCatalogCaller.class})
class S1ProductCatalogCallerCacheTests {

	private @MockitoBean S1Client client;
	private @Autowired S1ProductCatalogCaller caller;

	@Test
	void reusesTheCompletedCatalogOnSubsequentRequests() {
		var expected = List.<ProductRef>of(new S1Product("S1", "shared", "X1"), new S1Product("S1", "shared", "X2"));
		var response = new S1ProductsResponse(List.of(
			new S1ProductsResponse.ProductEntry(new S1ProductsResponse.Product("shared", "X1")),
			new S1ProductsResponse.ProductEntry(new S1ProductsResponse.Product("shared", "X2"))));
		given(client.getProducts()).willReturn(Mono.just(response));

		Mono.defer(() -> caller.getProducts().collectList()).repeat(1).as(StepVerifier::create)
			.expectNext(expected, expected).expectComplete().verify(Duration.ofSeconds(5));

		then(client).should().getProducts();
	}
}
