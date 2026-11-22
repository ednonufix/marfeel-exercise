package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import org.springframework.web.bind.annotation.BindParam;

public record CriteriaActiveProductRequest(
		@BindParam("service") String service,
		@BindParam("code") String code,
		@BindParam("subcode") String subcode,
		@BindParam("itemId") String itemId
) {
}
