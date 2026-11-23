package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record S2Request(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "S2", example = "S2") String service,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "S2-100") String itemId) implements ProductRefRequest {

	public S2Request {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("S2 requires itemId");
		}
	}
}
