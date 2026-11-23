package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

@Schema(description = "One result per distinct product; use ref to identify each result")
public record ProductSubscriptionResponse(List<Result> results) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Result(
			ProductRefResponse ref,
			SubscriptionStatus status,
			@Nullable @Schema(description = "Identifier assigned by the provider", example = "s1-123") String providerSubscriptionId,
			@Nullable @Schema(description = "Reason for a failed attempt", example = "user not eligible") String failureReason
	) {
	}

}
