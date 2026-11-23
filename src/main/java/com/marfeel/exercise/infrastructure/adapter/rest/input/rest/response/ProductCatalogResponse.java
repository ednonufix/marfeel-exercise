package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import com.marfeel.exercise.domain.vo.UserType;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Products available for the requested user, enriched with current metrics")
public record ProductCatalogResponse(UserType userId, List<ProductDetail> products) {

	public record ProductDetail(
	ProductRefResponse ref,
	@Schema(description = "Subscriber count from S3", example = "1280") int subscribers,
	@Schema(description = "IBAN from S3", example = "DE89370400440532013000") String iban
	) {

	}
}
