package com.marfeel.exercise.infrastructure.adapter.persistence.entity;

import com.marfeel.exercise.domain.vo.SubscriptionStatus;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

@Table("subscription_attempts")
public record SubscriptionAttemptEntity(
@Id @Nullable Long id,
@Column("user_id") String userId,
String service,
@Column("product_code") String productCode,
@Column("product_subcode") String productSubcode,
@Column("subscription_status") SubscriptionStatus subscriptionStatus,
@Column("failure_reason") @Nullable String failureReason,
@Column("attempted_at") LocalDateTime attemptedAt
) {
}
