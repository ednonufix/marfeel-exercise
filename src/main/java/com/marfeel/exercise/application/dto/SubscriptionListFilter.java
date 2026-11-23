package com.marfeel.exercise.application.dto;

import com.marfeel.exercise.domain.vo.UserType;

public record SubscriptionListFilter(
UserType userType,
CriteriaFilter criteria
) {
}
