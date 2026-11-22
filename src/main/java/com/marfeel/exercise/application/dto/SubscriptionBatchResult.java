package com.marfeel.exercise.application.dto;

import java.util.List;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

public record SubscriptionBatchResult(List<Result> results) {

	public record Result(
			ProductRef ref,
			SubscriptionStatus status,
			@Nullable String providerSubscriptionId,
			@Nullable String failureReason
	) {
	}

}
