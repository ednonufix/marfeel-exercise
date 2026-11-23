package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import org.springframework.web.bind.annotation.BindParam;

import io.swagger.v3.oas.annotations.media.Schema;

public record CriteriaActiveProductRequest(
@Schema(description = "Filter active subscriptions by provider") @BindParam("service") String service,
@Schema(description = "S1 product code") @BindParam("code") String code,
@Schema(description = "S1 product subcode") @BindParam("subcode") String subcode,
@Schema(description = "S2 item identifier") @BindParam("itemId") String itemId
) {
}
