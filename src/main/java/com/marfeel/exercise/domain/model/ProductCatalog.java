package com.marfeel.exercise.domain.model;

import com.marfeel.exercise.domain.vo.UserType;

import java.util.List;

public record ProductCatalog(
UserType userId,
List<ProductDetail> products
) {
}
