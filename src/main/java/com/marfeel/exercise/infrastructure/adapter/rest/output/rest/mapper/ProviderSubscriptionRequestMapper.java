package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper;

import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1SubscribeRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2SubscribeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProviderSubscriptionRequestMapper {

	@Mapping(target = "account", source = "userId")
	@Mapping(target = "product", source = "product")
	S1SubscribeRequest toS1Request(String userId, S1Product product);

	@Mapping(target = "user", source = "userId")
	@Mapping(target = "item", source = "product")
	S2SubscribeRequest toS2Request(String userId, S2Product product);

	@Mapping(target = "ref", source = "userId")
	S1SubscribeRequest.Account toAccount(String userId);

	S1SubscribeRequest.Product toProduct(S1Product product);

	S2SubscribeRequest.Item toItem(S2Product product);
}
