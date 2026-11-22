package com.marfeel.exercise.application.mapper;

import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.domain.model.ProductDetail;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SubscriptionMapper {

	SubscriptionWithDetail toDetail(Subscription subscription, ProductDetail detail);
}
