package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record S2Response(
@Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "S2", example = "S2") String service,
@Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "S2-100") String itemId) implements ProductRefResponse {
}
