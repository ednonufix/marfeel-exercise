package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.controller;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.dto.SubscriptionListFilter;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.application.dto.UserSubscriptions;
import com.marfeel.exercise.application.ports.input.ProductSubscriptionUseCase;
import com.marfeel.exercise.application.ports.input.UserSubscriptionsUseCase;
import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.mapper.ProductCatalogMapperImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@WebFluxTest(UserSubscriptionsRestController.class)
@Import({ProductCatalogMapperImpl.class, ControllerSecurityTestConfiguration.class})
class UserSubscriptionsRestControllerTests {

	private static final UserType USER = UserType.U1;
	private static final LocalDateTime DATE = LocalDateTime.of(2026, 1, 20, 12, 0);
	private static final S1Product S1 = new S1Product("S1", "A", "X1");
	private static final S2Product S2 = new S2Product("S2", "item-7");
	private static final SubscriptionWithDetail ACTIVE = new SubscriptionWithDetail(
		7L, new ProductDetail(S2, 4, "ES02"), "provider-7", DATE);
	private static final Attempt FAILED = new Attempt(8L, S1, SubscriptionStatus.FAILED, "rejected", DATE);

	private @Autowired WebTestClient client;

	private @MockitoBean UserSubscriptionsUseCase subscriptions;

	private @MockitoBean ProductSubscriptionUseCase subscribe;
	private WebTestClient userClient;

	@BeforeEach
	void setUp() {
		userClient = client.mutate().defaultHeader("API-Version", "v1")
			.defaultHeaders(headers -> headers.setBasicAuth("U1", "U1")).build();
	}

	@Test
	void bindsProductFiltersAndSerializesSubscriptionsAndAttempts() {
		assertDoesNotThrow(() -> {
			var filter = new SubscriptionListFilter(USER, new CriteriaFilter("S2", null, null, "item-7"));
			given(subscriptions.getSubscriptions(filter)).willReturn(
				Mono.just(new UserSubscriptions(List.of(ACTIVE), List.of(FAILED))));

			userClient.get().uri("/users/U1/subscriptions?service=S2&itemId=item-7")
				.exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.activeSubscription[0].detail.ref.itemId").isEqualTo("item-7")
				.jsonPath("$.activeSubscription[0].providerSubscriptionId").isEqualTo("provider-7")
				.jsonPath("$.attemptsSubscription[0].status").isEqualTo("FAILED")
				.jsonPath("$.attemptsSubscription[0].failureReason").isEqualTo("rejected");
		});
	}

	@Test
	void returnsTheRequestedSubscription() {
		assertDoesNotThrow(() -> {
			given(subscriptions.getSubscription(USER, 7)).willReturn(Mono.just(ACTIVE));

			userClient.get().uri("/users/U1/subscriptions/7").exchange()
				.expectStatus().isOk().expectBody()
				.jsonPath("$.id").isEqualTo(7)
				.jsonPath("$.detail.subscribers").isEqualTo(4);
		});
	}

	@Test
	void returns404WhenTheSubscriptionIsMissing() {
		assertDoesNotThrow(() -> {
			given(subscriptions.getSubscription(USER, 9)).willReturn(Mono.empty());

			userClient.get().uri("/users/U1/subscriptions/9").exchange().expectStatus().isNotFound();
		});
	}

	@Test
	void returnsRemainingSubscriptionsAfterDeletion() {
		assertDoesNotThrow(() -> {
			given(subscriptions.unsubscribe(USER, 9)).willReturn(Mono.just(List.of(ACTIVE)));

			userClient.delete().uri("/users/U1/subscriptions/9").exchange()
				.expectStatus().isOk().expectBody()
				.jsonPath("$.length()").isEqualTo(1)
				.jsonPath("$[0].id").isEqualTo(7);
		});
	}

	@Test
	void bindsAttemptFiltersAndReturnsMatchingHistory() {
		assertDoesNotThrow(() -> {
			given(subscriptions.findAttempts(USER, SubscriptionStatus.FAILED, "S1")).willReturn(Flux.just(FAILED));

			userClient.get().uri("/users/U1/subscription-attempts?status=FAILED&service=S1")
				.exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.length()").isEqualTo(1)
				.jsonPath("$[0].ref.subcode").isEqualTo("X1")
				.jsonPath("$[0].failureReason").isEqualTo("rejected");
		});
	}

	@Test
	void convertsBothRequestTypesAndReturnsTheirOutcomes() {
		var result = new SubscriptionBatchResult(List.of(new Result(S1, SubscriptionStatus.FAILED, null, "rejected"),
			new Result(S2, SubscriptionStatus.SUCCESS, "provider-7", null)));
		given(subscribe.subscribe(eq(USER), any())).willAnswer(call ->
			call.<Flux<ProductRef>>getArgument(1).collectList()
				.map(products -> {
					assertThat(products).containsExactly(S1, S2);
					return result;
				}));

		userClient.post().uri("/users/U1/subscriptions").contentType(APPLICATION_JSON)
			.bodyValue("""
				{"products":[{"service":"S1","code":"A","subcode":"X1"},{"service":"S2","itemId":"item-7"}]}
				""")
			.exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.results[0].failureReason").isEqualTo("rejected")
			.jsonPath("$.results[1].status").isEqualTo("SUCCESS")
			.jsonPath("$.results[1].providerSubscriptionId").isEqualTo("provider-7");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"""
			{"products":[]}
			""",
		"""
			{"products":[{"service":"S1","code":"A"}]}
			""",
		"""
			{"products":[{"service":"S2","itemId":" "}]}
			"""
	})
	void rejectsInvalidProductsBeforeCallingTheUseCase(String body) {
		userClient.post().uri("/users/U1/subscriptions").contentType(APPLICATION_JSON)
			.bodyValue(body).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(subscribe);
	}
}
