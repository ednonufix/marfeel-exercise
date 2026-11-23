package com.marfeel.exercise.application.dto;

import com.marfeel.exercise.domain.model.ProductDetail;

import java.time.LocalDateTime;

public record SubscriptionWithDetail(Long id, ProductDetail detail, String providerSubscriptionId, LocalDateTime subscribedAt) {
}
