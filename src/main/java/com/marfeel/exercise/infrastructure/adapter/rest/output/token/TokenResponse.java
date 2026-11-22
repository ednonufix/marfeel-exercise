package com.marfeel.exercise.infrastructure.adapter.rest.output.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
		String token,
		@JsonProperty("expires_in") int expiresIn) {
}
