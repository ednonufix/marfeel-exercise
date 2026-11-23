package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.application.mapper.SaveSubscriptionCommandMapper;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionClient;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.application.ports.output.SubscriptionProcessingFlow;
import com.marfeel.exercise.application.subscription.SubscriptionAttempt;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ProductSubscriptionServiceTests {
	@Test
	void savesFastProductWhileSlowProductIsPendingAndReturnsBothResults() {
		var slow = new S1Product("S1", "S1-A", "X1");
		var fast = new S2Product("S2", "S2-100");
		var pending = Sinks.<ProviderSubscriptionResult>one();
		List<ProductRef> saved = new ArrayList<>();
		var repository = new ProductSubscriptionRepository() {
			public Mono<Boolean> isActive(UserType user, ProductRef product) {
				return Mono.just(false);
			}

			public Mono<Void> save(SaveSubscriptionCommand command) {
				return Mono.fromRunnable(() -> saved.add(command.product()));
			}
		};
		var service = subscriptionService((user, product) -> Optional.of(() -> {
			}),
			(user, product) -> product.equals(slow) ? pending.asMono() : Mono.just(new ProviderSubscriptionResult("fast",
				null)),
			repository);

		service.subscribe(UserType.U3, Flux.just(slow, fast, fast)).as(StepVerifier::create)
			.then(() -> assertThat(saved).containsExactly(fast))
			.then(() -> pending.tryEmitValue(new ProviderSubscriptionResult("slow", null)))
			.assertNext(batch -> assertThat(batch.results()).extracting(Result::ref).containsExactlyInAnyOrder(slow, fast))
			.expectComplete().verify(Duration.ofSeconds(5));
	}

	@Test
	void reportsReservedAndActiveProductsWithoutCallingTheProvider() {
		var busy = new S1Product("S1", "A", "X1");
		var active = new S2Product("S2", "item-7");
		var repository = mock(ProductSubscriptionRepository.class);
		var provider = mock(ProductSubscriptionClient.class);
		var reservation = mock(SubscriptionProcessingFlow.Reservation.class);
		given(repository.isActive(UserType.U1, active)).willReturn(Mono.just(true));
		var service = subscriptionService(
			(user, product) -> product.equals(busy) ? Optional.empty() : Optional.of(reservation),
			provider, repository);

		service.subscribe(UserType.U1, Flux.just(busy, active)).as(StepVerifier::create)
			.assertNext(batch -> assertThat(batch.results()).containsExactlyInAnyOrder(
				new Result(busy, SubscriptionStatus.IN_PROGRESS, null, null),
				new Result(active, SubscriptionStatus.ALREADY_SUBSCRIBED, null, null)))
			.expectComplete().verify(Duration.ofSeconds(5));

		verifyNoInteractions(provider);
		then(repository).should().isActive(UserType.U1, active);
		verifyNoMoreInteractions(repository);
		then(reservation).should().release();
	}

	@Test
	void persistsProviderRejectionsErrorsAndEmptyResponsesAsFailedAttempts() {
		var rejected = new S1Product("S1", "A", "X1");
		var errored = new S2Product("S2", "item-7");
		var empty = new S2Product("S2", "item-8");
		var repository = mock(ProductSubscriptionRepository.class);
		var provider = mock(ProductSubscriptionClient.class);
		var reservation = mock(SubscriptionProcessingFlow.Reservation.class);
		given(repository.isActive(eq(UserType.U1), any())).willReturn(Mono.just(false));
		given(repository.save(any())).willReturn(Mono.empty());
		given(provider.subscribe(UserType.U1, rejected)).willReturn(Mono.just(new ProviderSubscriptionResult(null, "rejected")));
		given(provider.subscribe(UserType.U1, errored)).willReturn(Mono.error(new IllegalStateException("offline")));
		given(provider.subscribe(UserType.U1, empty)).willReturn(Mono.empty());
		var service = subscriptionService((user, product) -> Optional.of(reservation),
			provider, repository);

		service.subscribe(UserType.U1, Flux.just(rejected, errored, empty)).as(StepVerifier::create)
			.assertNext(batch -> assertThat(batch.results()).containsExactlyInAnyOrder(
				new Result(rejected, SubscriptionStatus.FAILED, null, "rejected"),
				new Result(errored, SubscriptionStatus.FAILED, null, "offline"),
				new Result(empty, SubscriptionStatus.FAILED, null, "Not provided error message")))
			.expectComplete().verify(Duration.ofSeconds(5));

		then(repository).should()
			.save(new SaveSubscriptionCommand(UserType.U1, rejected, SubscriptionStatus.FAILED, null, "rejected"));
		then(repository).should().save(new SaveSubscriptionCommand(UserType.U1, errored, SubscriptionStatus.FAILED, null,
			"offline"));
		then(repository).should()
			.save(new SaveSubscriptionCommand(UserType.U1, empty, SubscriptionStatus.FAILED, null, "Not provided error "
				+ "message"));
		then(reservation).should(times(3)).release();
	}

	@Test
	void holdsReservationUntilPersistenceCompletes() {
		var product = new S2Product("S2", "item-7");
		var repository = mock(ProductSubscriptionRepository.class);
		var reservation = mock(SubscriptionProcessingFlow.Reservation.class);
		var persistence = Sinks.<Void>empty();
		var command = new SaveSubscriptionCommand(UserType.U1, product, SubscriptionStatus.SUCCESS, "provider-id", null);
		given(repository.isActive(UserType.U1, product)).willReturn(Mono.just(false));
		given(repository.save(command)).willReturn(persistence.asMono());
		var service = subscriptionService((user, ref) -> Optional.of(reservation),
			(user, ref) -> Mono.just(new ProviderSubscriptionResult("provider-id", null)), repository);

		service.subscribe(UserType.U1, Flux.just(product)).as(StepVerifier::create)
			.then(() -> {
				then(repository).should().save(command);
				verifyNoInteractions(reservation);
			})
			.then(() -> persistence.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
			.assertNext(batch -> assertThat(batch.results())
				.containsExactly(new Result(product, SubscriptionStatus.SUCCESS, "provider-id", null)))
			.expectComplete().verify(Duration.ofSeconds(5));

		then(reservation).should().release();
	}

	private ProductSubscriptionService subscriptionService(SubscriptionProcessingFlow processing,
		ProductSubscriptionClient provider, ProductSubscriptionRepository repository) {
		var mapper = Mappers.getMapper(SaveSubscriptionCommandMapper.class);
		var attempt = new SubscriptionAttempt(provider, repository, mapper);
		return new ProductSubscriptionService(processing, repository, mapper, attempt);
	}
}
