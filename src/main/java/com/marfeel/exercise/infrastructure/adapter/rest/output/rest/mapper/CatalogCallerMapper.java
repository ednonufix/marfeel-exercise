package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper;

import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2MetricResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CatalogCallerMapper {

	@Mapping(target = "ref", source = ".")
	ProductDetail toDomain(S1MetricResponse response);

	@Mapping(target = "ref", source = ".")
	ProductDetail toDomain(S2MetricResponse response);

	S1Product toRef(S1MetricResponse response);

	S2Product toRef(S2MetricResponse response);
}
