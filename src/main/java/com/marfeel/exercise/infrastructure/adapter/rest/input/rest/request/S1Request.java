package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

public record S1Request(
		String service,
		String code,
		String subcode) implements ProductRefRequest {

	public S1Request {
		if (code == null || code.isBlank() || subcode == null || subcode.isBlank()) {
			throw new IllegalArgumentException("S1 requires code and subcode");
		}
	}
}
