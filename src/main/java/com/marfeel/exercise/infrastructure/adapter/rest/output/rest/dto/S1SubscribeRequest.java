package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

public record S1SubscribeRequest(Account account, Product product) {
	public record Account(String ref) {
	}

	public record Product(String code, String subcode) {
	}
}
