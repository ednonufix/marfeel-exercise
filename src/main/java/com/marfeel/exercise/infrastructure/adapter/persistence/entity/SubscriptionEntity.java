package com.marfeel.exercise.infrastructure.adapter.persistence.entity;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("subscriptions")
public record SubscriptionEntity(
		@Id @Nullable Long id,
		@Column("user_id") String userId,
		String service,
		@Column("product_code") String productCode,
		@Column("product_subcode") String productSubcode,
		@Column("provider_subscription_id") String providerSubscriptionId,
		@Column("subscribed_at") LocalDateTime subscribedAt
) {
}
