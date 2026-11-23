package com.marfeel.exercise.application.dto;

import com.marfeel.exercise.domain.model.ProductRef;

import java.time.LocalDateTime;

public record Subscription(Long id, ProductRef ref, String providerSubscriptionId, LocalDateTime subscribedAt) {
}
