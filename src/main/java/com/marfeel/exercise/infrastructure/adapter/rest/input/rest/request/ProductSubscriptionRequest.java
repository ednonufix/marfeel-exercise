package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Products to subscribe to in a single batch; repeated references are processed once", example = """
	{"products":[{"service":"S1","code":"S1-A","subcode":"X1"},{"service":"S2","itemId":"S2-100"}]}
	""")
public record ProductSubscriptionRequest(
@NotEmpty List<@NotNull ProductRefRequest> products
) {
}
