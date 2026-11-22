package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import java.util.List;

import com.marfeel.exercise.domain.vo.UserType;

public record ProductCatalogResponse(UserType userId, List<ProductDetail> products) {

	public record ProductDetail(
			ProductRefResponse ref,
			int subscribers,
			String iban
	) {

	}
}
