package com.marfeel.exercise.application.service;

import java.time.Duration;

import com.marfeel.exercise.application.ports.output.ProductCatalogRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class FilterUnsubscribedProductsServiceTests {

	@Test
	void excludesOnlyExactActiveReferencesForTheRequestedUser() {
		var active = new S1Product("S1", "shared", "X1");
		var otherSubcode = new S1Product("S1", "shared", "X2");
		var otherProvider = new S2Product("S2", "shared");
		var repository = mock(ProductCatalogRepository.class);
		given(repository.findActiveProductRefs(UserType.U2)).willReturn(Flux.just(active, active));
		var service = new FilterUnsubscribedProductsService(repository);

		service.filter(UserType.U2, Flux.just(active, otherSubcode, otherProvider, active))
				.as(StepVerifier::create).expectNext(otherSubcode, otherProvider)
				.expectComplete().verify(Duration.ofSeconds(5));

		then(repository).should().findActiveProductRefs(UserType.U2);
	}

	@Test
	void propagatesRepositoryFailureBeforeSubscribingToTheCatalog() {
		var failure = new IllegalStateException("catalog unavailable");
		var service = new FilterUnsubscribedProductsService(user -> Flux.error(failure));
		var catalog = PublisherProbe.of(Flux.<ProductRef>just(new S2Product("S2", "item")));

		service.filter(UserType.U1, catalog.flux()).as(StepVerifier::create)
				.expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
				.verify(Duration.ofSeconds(5));

		catalog.assertWasNotSubscribed();
	}
}
