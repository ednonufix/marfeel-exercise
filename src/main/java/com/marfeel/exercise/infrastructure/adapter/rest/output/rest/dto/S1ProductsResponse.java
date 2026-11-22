package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

import java.util.List;

public record S1ProductsResponse(List<ProductEntry> products) {
	public record ProductEntry(Product product) {
	}

	public record Product(String code, String subcode) {
	}
}
