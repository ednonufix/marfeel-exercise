package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record S1Response(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "S1", example = "S1") String service,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "S1-A") String code,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "X1") String subcode) implements ProductRefResponse {
}
