package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

public record S2SubscribeRequest(String user, Item item) {

	public record Item(String itemId) {
	}
}
