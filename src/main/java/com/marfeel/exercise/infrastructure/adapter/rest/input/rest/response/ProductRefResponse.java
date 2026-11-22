package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "service"
)
@JsonSubTypes({
		@JsonSubTypes.Type(
				value = S1Response.class,
				name = "S1"
		),
		@JsonSubTypes.Type(
				value = S2Response.class,
				name = "S2"
		)
})
public sealed interface ProductRefResponse permits S1Response,
                                                   S2Response {
}
