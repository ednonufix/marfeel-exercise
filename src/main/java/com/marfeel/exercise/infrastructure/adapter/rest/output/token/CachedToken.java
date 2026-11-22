package com.marfeel.exercise.infrastructure.adapter.rest.output.token;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

public record CachedToken(@NonNull String token, @NonNull Duration ttl) implements Token {
}
