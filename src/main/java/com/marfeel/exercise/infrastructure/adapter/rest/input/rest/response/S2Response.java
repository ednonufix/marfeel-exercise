package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

public record S2Response(
		String service,
		String itemId) implements ProductRefResponse {
}
