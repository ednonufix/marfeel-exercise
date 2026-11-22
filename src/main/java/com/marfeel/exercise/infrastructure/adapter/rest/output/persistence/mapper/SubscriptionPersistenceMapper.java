package com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.entity.SubscriptionAttemptEntity;
import com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.entity.SubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SubscriptionPersistenceMapper {

	default SubscriptionAttemptEntity toAttempt(SaveSubscriptionCommand command) {
		var attemptedAt = LocalDateTime.now(ZoneOffset.UTC);
		return switch (command.product()) {
			case S1Product s1 -> toAttempt(command, s1, attemptedAt);
			case S2Product s2 -> toAttempt(command, s2, attemptedAt);
		};
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", source = "command.userType")
	@Mapping(target = "productCode", source = "product.code")
	@Mapping(target = "productSubcode", source = "product.subcode")
	@Mapping(target = "subscriptionStatus", source = "command.status")
	SubscriptionAttemptEntity toAttempt(SaveSubscriptionCommand command, S1Product product, LocalDateTime attemptedAt);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", source = "command.userType")
	@Mapping(target = "productCode", source = "product.itemId")
	@Mapping(target = "productSubcode", expression = "java(\"\")")
	@Mapping(target = "subscriptionStatus", source = "command.status")
	SubscriptionAttemptEntity toAttempt(SaveSubscriptionCommand command, S2Product product, LocalDateTime attemptedAt);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "subscribedAt", source = "attempt.attemptedAt")
	SubscriptionEntity toSubscription(SubscriptionAttemptEntity attempt, String providerSubscriptionId);

	@Mapping(target = "ref", source = "entity")
	Subscription toSubscription(SubscriptionEntity entity);

	@Mapping(target = "ref", source = "entity")
	@Mapping(target = "status", source = "subscriptionStatus")
	Attempt toAttempt(SubscriptionAttemptEntity entity);

	default ProductRef toProductRef(SubscriptionEntity entity) {
		return toProductRef(entity.service(), entity.productCode(), entity.productSubcode());
	}

	default ProductRef toProductRef(SubscriptionAttemptEntity entity) {
		return toProductRef(entity.service(), entity.productCode(), entity.productSubcode());
	}

	private ProductRef toProductRef(String service, String code, String subcode) {
		return switch (service) {
			case "S1" -> new S1Product(service, code, subcode);
			case "S2" -> new S2Product(service, code);
			default -> throw new IllegalStateException("Unknown subscription service: " + service);
		};
	}
}
