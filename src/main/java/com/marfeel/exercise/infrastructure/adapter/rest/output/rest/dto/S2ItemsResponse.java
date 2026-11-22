package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

import java.util.List;

public record S2ItemsResponse(List<ItemEntry> items) {
	public record ItemEntry(Item item) {
	}

	public record Item(String itemId) {
	}
}
