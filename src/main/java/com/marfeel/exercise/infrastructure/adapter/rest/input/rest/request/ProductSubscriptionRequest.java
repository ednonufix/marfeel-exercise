package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductSubscriptionRequest(
		@NotEmpty List<@NotNull ProductRefRequest> products
) {
}
