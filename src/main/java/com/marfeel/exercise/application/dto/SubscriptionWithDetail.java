package com.marfeel.exercise.application.dto;

import java.time.LocalDateTime;

import com.marfeel.exercise.domain.model.ProductDetail;

public record SubscriptionWithDetail(Long id, ProductDetail detail, String providerSubscriptionId, LocalDateTime subscribedAt) {
}
