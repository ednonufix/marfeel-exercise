package com.marfeel.exercise.application.dto;

import java.time.LocalDateTime;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

public record Attempt(Long id, ProductRef ref, SubscriptionStatus status, @Nullable String failureReason,
                      LocalDateTime attemptedAt) {
}
