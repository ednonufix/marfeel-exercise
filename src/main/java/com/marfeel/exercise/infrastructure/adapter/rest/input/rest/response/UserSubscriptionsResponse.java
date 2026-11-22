package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

public record UserSubscriptionsResponse(List<Active> activeSubscription, List<Attempt> attemptsSubscription) {

	public record Active(
			Long id,
			ProductCatalogResponse.ProductDetail detail,
			String providerSubscriptionId,
			LocalDateTime subscribedAt
	) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Attempt(
			Long id,
			ProductRefResponse ref,
			SubscriptionStatus status,
			@Nullable String failureReason,
			LocalDateTime attemptedAt
	) {
	}
}
