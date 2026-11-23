package com.marfeel.exercise.application.dto;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

public record Attempt(Long id, ProductRef ref, SubscriptionStatus status, @Nullable String failureReason,
LocalDateTime attemptedAt) {
}
