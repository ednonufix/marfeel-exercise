package com.marfeel.exercise.application.dto;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record SaveSubscriptionCommand(
@NonNull UserType userType,
@NonNull ProductRef product,
@NonNull SubscriptionStatus status,
@Nullable String providerSubscriptionId,
@Nullable String failureReason
) {

}
