package com.marfeel.exercise.application.dto;

import org.jspecify.annotations.Nullable;

public record ProviderSubscriptionResult(
		@Nullable String subscriptionId,
		@Nullable String message
) {
}
