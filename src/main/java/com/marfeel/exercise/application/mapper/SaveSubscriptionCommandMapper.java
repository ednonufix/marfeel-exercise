package com.marfeel.exercise.application.mapper;

import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult.Result;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SaveSubscriptionCommandMapper {

	@Mapping(target = "failureReason", ignore = true)
	@Mapping(target = "providerSubscriptionId", source = "providerId")
	@Mapping(target = "status", constant = "SUCCESS")
	SaveSubscriptionCommand toSuccess(UserType userType, ProductRef product, String providerId);

	@Mapping(target = "providerSubscriptionId", ignore = true)
	@Mapping(target = "status", constant = "FAILED")
	SaveSubscriptionCommand toFailure(UserType userType, ProductRef product, String failureReason);

	@Mapping(target = "ref", source = "product")
	Result toResult(SaveSubscriptionCommand command);

	@Mapping(target = "ref", source = "ref")
	@Mapping(target = "status", constant = "ALREADY_SUBSCRIBED")
	@Mapping(target = "providerSubscriptionId", ignore = true)
	@Mapping(target = "failureReason", ignore = true)
	Result toAlreadySubscribed(ProductRef ref);

}
