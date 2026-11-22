package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

public record S1Response(
		String service,
		String code,
		String subcode) implements ProductRefResponse {
}
