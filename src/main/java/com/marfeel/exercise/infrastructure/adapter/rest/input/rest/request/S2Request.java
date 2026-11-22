package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

public record S2Request(
		String service,
		String itemId) implements ProductRefRequest {

	public S2Request {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("S2 requires itemId");
		}
	}
}
