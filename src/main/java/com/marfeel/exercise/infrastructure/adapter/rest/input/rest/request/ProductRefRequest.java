package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "service",
		visible = true
)
@JsonSubTypes({
		@JsonSubTypes.Type(
				value = S1Request.class,
				name = "S1"
		),
		@JsonSubTypes.Type(
				value = S2Request.class,
				name = "S2"
		)
})
public sealed interface ProductRefRequest permits S1Request,
                                                  S2Request {
}
