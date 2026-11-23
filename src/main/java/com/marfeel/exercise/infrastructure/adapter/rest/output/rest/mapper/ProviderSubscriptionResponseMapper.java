package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProviderSubscriptionResponseMapper {

	default ProviderSubscriptionResult fromS1(S1SubscribeResponse response) {
		return "ACTIVE".equals(response.state())
			? confirmed(response.subscriptionId())
			: rejected("S1 did not confirm the subscription");
	}

	default ProviderSubscriptionResult fromS2(S2SubscribeResponse response) {
		if ("OK".equals(response.status())) {
			return confirmed(response.subscriptionId());
		}
		var reason = response.reason();
		return rejected(reason == null || reason.isBlank() ? "S2 rejected the subscription" : reason);
	}

	private ProviderSubscriptionResult confirmed(String subscriptionId) {
		return accepted(subscriptionId);
	}

	@Mapping(target = "subscriptionId", source = "subscriptionId")
	@Mapping(target = "message", ignore = true)
	ProviderSubscriptionResult accepted(String subscriptionId);

	@Mapping(target = "subscriptionId", ignore = true)
	@Mapping(target = "message", source = "message")
	ProviderSubscriptionResult rejected(String message);
}
