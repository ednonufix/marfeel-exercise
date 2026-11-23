package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Provider-specific product identity", oneOf = {S1Request.class, S2Request.class},
		discriminatorProperty = "service", discriminatorMapping = {
		@DiscriminatorMapping(value = "S1", schema = S1Request.class),
		@DiscriminatorMapping(value = "S2", schema = S2Request.class)})
public sealed interface ProductRefRequest permits S1Request,
                                                  S2Request {
}
