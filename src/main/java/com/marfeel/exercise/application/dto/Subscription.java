package com.marfeel.exercise.application.dto;

import java.time.LocalDateTime;

import com.marfeel.exercise.domain.model.ProductRef;

public record Subscription(Long id, ProductRef ref, String providerSubscriptionId, LocalDateTime subscribedAt) {
}
