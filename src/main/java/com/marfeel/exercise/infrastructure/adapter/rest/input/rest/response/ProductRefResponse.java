package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY,
	property = "service"
)
@JsonSubTypes( {
	@JsonSubTypes.Type(
		value = S1Response.class,
		name = "S1"
	),
	@JsonSubTypes.Type(
		value = S2Response.class,
		name = "S2"
	)
})
@Schema(description = "Provider-specific product identity", oneOf = {S1Response.class, S2Response.class},
	discriminatorProperty = "service", discriminatorMapping = {
	@DiscriminatorMapping(value = "S1", schema = S1Response.class),
	@DiscriminatorMapping(value = "S2", schema = S2Response.class)})
public sealed interface ProductRefResponse permits S1Response,
	S2Response {
}
