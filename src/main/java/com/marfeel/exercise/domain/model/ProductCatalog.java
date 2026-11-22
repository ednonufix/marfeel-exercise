package com.marfeel.exercise.domain.model;

import java.util.List;

import com.marfeel.exercise.domain.vo.UserType;

public record ProductCatalog(
		UserType userId,
		List<ProductDetail> products
) {
}
