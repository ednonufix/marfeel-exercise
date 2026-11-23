package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2ItemsResponse;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S2ProductCatalogCallerTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private @Mock S2Client client;
	private S2ProductCatalogCaller caller;

	@BeforeEach
	void setUp() {
		caller = new S2ProductCatalogCaller(client);
	}

	@Test
	void mapsS2ItemEntries() {
		given(client.getItems()).willReturn(Mono.just(new S2ItemsResponse(List.of(s2Entry("item-7"), s2Entry("item-8")))));

		caller.getProducts().as(StepVerifier::create)
			.expectNext(new S2Product("S2", "item-7"), new S2Product("S2", "item-8"))
			.expectComplete().verify(TIMEOUT);
	}

	private static S2ItemsResponse.ItemEntry s2Entry(String itemId) {
		return new S2ItemsResponse.ItemEntry(new S2ItemsResponse.Item(itemId));
	}
}
