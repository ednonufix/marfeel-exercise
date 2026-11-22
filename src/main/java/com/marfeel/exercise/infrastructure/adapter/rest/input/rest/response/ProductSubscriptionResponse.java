package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

public record ProductSubscriptionResponse(List<Result> results) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Result(
			ProductRefResponse ref,
			SubscriptionStatus status,
			@Nullable String providerSubscriptionId,
			@Nullable String failureReason
	) {
	}

}
