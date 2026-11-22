package com.marfeel.exercise.application.dto;

import java.util.List;

public record UserSubscriptions(List<SubscriptionWithDetail> activeSubscription, List<Attempt> attemptsSubscription) {
}
