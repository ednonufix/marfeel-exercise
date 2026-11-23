package com.marfeel.exercise.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.application.dto.SubscriptionListFilter;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.application.dto.UserSubscriptions;
import com.marfeel.exercise.application.mapper.SubscriptionMapper;
import com.marfeel.exercise.application.ports.output.ProductMetricsClient;
import com.marfeel.exercise.application.ports.output.UserSubscriptionsRepository;
import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionsServiceTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final UserType USER = UserType.U2;
	private static final LocalDateTime DATE = LocalDateTime.of(2026, 1, 20, 12, 0);
	private static final S2Product PRODUCT = new S2Product("S2", "item-7");
	private static final Subscription ACTIVE = new Subscription(7L, PRODUCT, "provider-7", DATE);
	private static final ProductDetail METRICS = new ProductDetail(PRODUCT, 12, "ES01");
	private static final SubscriptionWithDetail ENRICHED = new SubscriptionWithDetail(7L, METRICS, "provider-7", DATE);
	private static final Attempt FAILED = new Attempt(9L, PRODUCT, SubscriptionStatus.FAILED, "rejected", DATE);

	private @Mock UserSubscriptionsRepository repository;
	private @Mock ProductMetricsClient metrics;
	private UserSubscriptionsService service;

	@BeforeEach
	void setUp() {
		service = new UserSubscriptionsService(
				metrics, repository, Mappers.getMapper(SubscriptionMapper.class));
	}

	@Test
	void filtersActiveSubscriptionsButReturnsTheFullAttemptHistory() {
		var criteria = new CriteriaFilter("S2", null, null, "item-7");
		given(repository.findActiveSubscriptions(USER, criteria)).willReturn(Flux.just(ACTIVE));
		given(repository.findAttempts(USER, null, null)).willReturn(Flux.just(FAILED));
		given(metrics.getMetrics(PRODUCT)).willReturn(Mono.just(METRICS));

		service.getSubscriptions(new SubscriptionListFilter(USER, criteria)).as(StepVerifier::create)
				.expectNext(new UserSubscriptions(List.of(ENRICHED), List.of(FAILED)))
				.expectComplete().verify(TIMEOUT);
	}

	@Test
	void enrichesASingleActiveSubscription() {
		given(repository.findActiveSubscription(USER, 7L)).willReturn(Mono.just(ACTIVE));
		given(metrics.getMetrics(PRODUCT)).willReturn(Mono.just(METRICS));

		service.getSubscription(USER, 7L).as(StepVerifier::create)
				.expectNext(ENRICHED).expectComplete().verify(TIMEOUT);
	}

	@Test
	void doesNotRequestMetricsForAMissingSubscription() {
		given(repository.findActiveSubscription(USER, 7L)).willReturn(Mono.empty());

		service.getSubscription(USER, 7L).as(StepVerifier::create).expectComplete().verify(TIMEOUT);

		verifyNoInteractions(metrics);
	}

	@Test
	void waitsForDeletionBeforeReadingAndEnrichingRemainingSubscriptions() {
		var deletion = TestPublisher.<Void>create();
		var remaining = PublisherProbe.of(Flux.just(ACTIVE));
		given(repository.deleteActiveSubscription(USER, 42L)).willReturn(deletion.mono());
		given(repository.findActiveSubscriptions(USER, null)).willReturn(remaining.flux());
		given(metrics.getMetrics(PRODUCT)).willReturn(Mono.just(METRICS));

		service.unsubscribe(USER, 42L).as(StepVerifier::create)
				.then(remaining::assertWasNotSubscribed)
				.then(deletion::complete)
				.expectNext(List.of(ENRICHED)).expectComplete().verify(TIMEOUT);
	}

	@Test
	void forwardsAttemptFiltersWithoutRequestingMetrics() {
		given(repository.findAttempts(USER, SubscriptionStatus.FAILED, "S2")).willReturn(Flux.just(FAILED));

		service.findAttempts(USER, SubscriptionStatus.FAILED, "S2").as(StepVerifier::create)
				.expectNext(FAILED).expectComplete().verify(TIMEOUT);

		verifyNoInteractions(metrics);
	}
}
