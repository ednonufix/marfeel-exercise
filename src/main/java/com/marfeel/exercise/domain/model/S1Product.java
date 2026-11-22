package com.marfeel.exercise.domain.model;

public record S1Product(
		String service,
		String code,
		String subcode) implements ProductRef {
}
