package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import java.time.Duration;

import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductCatalogCallerTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private @Mock S1ProductCatalogCaller s1;
	private @Mock S2ProductCatalogCaller s2;
	private ProductCatalogCaller caller;

	@BeforeEach
	void setUp() {
		caller = new ProductCatalogCaller(s1, s2);
	}

	@Test
	void mergesBothCatalogsWithoutCollapsingReferencesWithTheSameIdentifier() {
		var s1Product = new S1Product("S1", "shared", "X1");
		var s2Product = new S2Product("S2", "shared");
		given(s1.getProducts()).willReturn(Flux.just(s1Product));
		given(s2.getProducts()).willReturn(Flux.just(s2Product));

		caller.allProducts().collectList().as(StepVerifier::create)
				.assertNext(products -> assertThat(products).containsExactlyInAnyOrder(
						new S1Product("S1", "shared", "X1"), new S2Product("S2", "shared")))
				.expectComplete().verify(TIMEOUT);
	}

}
