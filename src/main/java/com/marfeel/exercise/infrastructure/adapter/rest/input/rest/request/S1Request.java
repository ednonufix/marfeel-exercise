package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record S1Request(
@Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "S1", example = "S1") String service,
@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "S1-A") String code,
@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "X1") String subcode) implements ProductRefRequest {

	public S1Request {
		if (code == null || code.isBlank() || subcode == null || subcode.isBlank()) {
			throw new IllegalArgumentException("S1 requires code and subcode");
		}
	}
}
