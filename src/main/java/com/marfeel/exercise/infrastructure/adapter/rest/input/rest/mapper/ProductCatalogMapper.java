package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.mapper;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.SubscriptionBatchResult;
import com.marfeel.exercise.application.dto.SubscriptionListFilter;
import com.marfeel.exercise.application.dto.SubscriptionWithDetail;
import com.marfeel.exercise.application.dto.UserSubscriptions;
import com.marfeel.exercise.domain.model.ProductCatalog;
import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.CriteriaActiveProductRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.ProductRefRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.S1Request;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.S2Request;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductCatalogResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductRefResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductSubscriptionResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.S1Response;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.S2Response;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.UserSubscriptionsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductCatalogMapper {

	ProductCatalogResponse fromDomain(ProductCatalog domain);

	ProductCatalogResponse.ProductDetail toDetail(ProductDetail detail);

	ProductSubscriptionResponse fromDomain(SubscriptionBatchResult domain);

	UserSubscriptionsResponse fromDomain(UserSubscriptions domain);

	UserSubscriptionsResponse.Active fromDomain(SubscriptionWithDetail domain);

	SubscriptionListFilter toSubscriptionList(UserType userType, CriteriaActiveProductRequest criteria);

	@SubclassMapping(target = S1Response.class, source = S1Product.class)
	@SubclassMapping(target = S2Response.class, source = S2Product.class)
	ProductRefResponse toRef(ProductRef ref);

	@SubclassMapping(target = S1Product.class, source = S1Request.class)
	@SubclassMapping(target = S2Product.class, source = S2Request.class)
	ProductRef toRef(ProductRefRequest ref);


	UserSubscriptionsResponse.Attempt fromDomain(Attempt attempt);
}
