package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import java.time.Duration;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.ProviderSubscriptionRequestMapper;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.ProviderSubscriptionResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductSubscriptionCallerTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private @Mock S1Client s1;
	private @Mock S2Client s2;
	private ProductSubscriptionCaller caller;

	@BeforeEach
	void setUp() {
		caller = new ProductSubscriptionCaller(s1, s2,
				Mappers.getMapper(ProviderSubscriptionRequestMapper.class),
				Mappers.getMapper(ProviderSubscriptionResponseMapper.class));
	}

	@Test
	void submitsS1AccountAndProductAndMapsConfirmation() {
		var request = new S1SubscribeRequest(new S1SubscribeRequest.Account("U2"), new S1SubscribeRequest.Product("A", "X2"));
		given(s1.subscribe(request)).willReturn(Mono.just(new S1SubscribeResponse("confirmation", "ACTIVE")));

		caller.subscribe(UserType.U2, new S1Product("S1", "A", "X2")).as(StepVerifier::create)
				.expectNext(new ProviderSubscriptionResult("confirmation", null)).expectComplete().verify(TIMEOUT);

		verifyNoInteractions(s2);
	}

	@Test
	void submitsS2UserAndItemAndKeepsRejectionReason() {
		var request = new S2SubscribeRequest("U3", new S2SubscribeRequest.Item("item-7"));
		given(s2.subscribe(request)).willReturn(Mono.just(new S2SubscribeResponse(null, "FAILED", "not eligible")));

		caller.subscribe(UserType.U3, new S2Product("S2", "item-7")).as(StepVerifier::create)
				.expectNext(new ProviderSubscriptionResult(null, "not eligible")).expectComplete().verify(TIMEOUT);

		verifyNoInteractions(s1);
	}

	@Test
	void suppliesFallbackWhenS2RejectsWithoutAReason() {
		var request = new S2SubscribeRequest("U3", new S2SubscribeRequest.Item("item-7"));
		given(s2.subscribe(request)).willReturn(Mono.just(new S2SubscribeResponse(null, "FAILED", " ")));

		caller.subscribe(UserType.U3, new S2Product("S2", "item-7")).as(StepVerifier::create)
				.expectNext(new ProviderSubscriptionResult(null, "S2 rejected the subscription"))
				.expectComplete().verify(TIMEOUT);
	}
}
