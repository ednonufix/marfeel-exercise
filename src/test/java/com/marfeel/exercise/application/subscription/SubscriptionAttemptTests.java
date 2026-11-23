package com.marfeel.exercise.application.subscription;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.mapper.SaveSubscriptionCommandMapper;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionClient;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SubscriptionAttemptTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final UserType USER = UserType.U1;
	private static final S1Product PRODUCT = new S1Product("S1", "A", "X1");

	private @Mock ProductSubscriptionClient provider;
	private @Mock ProductSubscriptionRepository repository;
	private SubscriptionAttempt attempt;

	@BeforeEach
	void setUp() {
		attempt = new SubscriptionAttempt(provider, repository, Mappers.getMapper(SaveSubscriptionCommandMapper.class));
	}

	@Test
	void callsTheProviderOnSubscriptionAndEmitsSuccessAfterPersistenceCompletes() {
		var persistence = TestPublisher.<Void>create();
		var command = new SaveSubscriptionCommand(USER, PRODUCT, SubscriptionStatus.SUCCESS, "provider-id", null);
		given(provider.subscribe(USER, PRODUCT)).willReturn(Mono.just(new ProviderSubscriptionResult("provider-id", null)));
		given(repository.save(command)).willReturn(persistence.mono());

		var outcome = attempt.execute(USER, PRODUCT);
		then(provider).shouldHaveNoInteractions();
		then(repository).shouldHaveNoInteractions();

		StepVerifier.create(outcome).expectSubscription()
			.expectNoEvent(Duration.ofMillis(20))
			.then(persistence::assertWasSubscribed)
			.then(persistence::complete)
			.expectNext(new Result(PRODUCT, SubscriptionStatus.SUCCESS, "provider-id", null))
			.expectComplete().verify(TIMEOUT);

		then(provider).should().subscribe(USER, PRODUCT);
		then(repository).should().save(command);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("failedOutcomes")
	void persistsProviderFailuresAndReturnsTheirReason(String scenario, Mono<ProviderSubscriptionResult> response,
		String reason) {
		var command = new SaveSubscriptionCommand(USER, PRODUCT, SubscriptionStatus.FAILED, null, reason);
		given(provider.subscribe(USER, PRODUCT)).willReturn(response);
		given(repository.save(command)).willReturn(Mono.empty());

		StepVerifier.create(attempt.execute(USER, PRODUCT))
			.expectNext(new Result(PRODUCT, SubscriptionStatus.FAILED, null, reason))
			.expectComplete().verify(TIMEOUT);

		then(repository).should().save(command);
	}

	@Test
	void propagatesPersistenceErrorsWithoutEmittingTheProviderResult() {
		var failure = new IllegalStateException("database unavailable");
		var command = new SaveSubscriptionCommand(USER, PRODUCT, SubscriptionStatus.SUCCESS, "provider-id", null);
		given(provider.subscribe(USER, PRODUCT)).willReturn(Mono.just(new ProviderSubscriptionResult("provider-id", null)));
		given(repository.save(command)).willReturn(Mono.error(failure));

		StepVerifier.create(attempt.execute(USER, PRODUCT))
			.expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
			.verify(TIMEOUT);
	}

	private static Stream<Arguments> failedOutcomes() {
		return Stream.of(
			Arguments.of("provider rejection", Mono.just(new ProviderSubscriptionResult(null, "rejected")), "rejected"),
			Arguments.of("provider error", Mono.error(new IllegalStateException("offline")), "offline"),
			Arguments.of("empty response", Mono.empty(), "Not provided error message"),
			Arguments.of("rejection without a reason", Mono.just(new ProviderSubscriptionResult(null, null)),
				"Not provided error message"));
	}
}
