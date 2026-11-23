package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

@Schema(description = "Active subscriptions with metrics and the full history of subscription attempts")
public record UserSubscriptionsResponse(List<Active> activeSubscription, List<Attempt> attemptsSubscription) {

	public record Active(
			Long id,
			ProductCatalogResponse.ProductDetail detail,
			@Schema(description = "Identifier assigned by the provider", example = "s1-123") String providerSubscriptionId,
			LocalDateTime subscribedAt
	) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Attempt(
			Long id,
			ProductRefResponse ref,
			SubscriptionStatus status,
			@Nullable @Schema(description = "Reason for a failed attempt", example = "user not eligible") String failureReason,
			LocalDateTime attemptedAt
	) {
	}
}
