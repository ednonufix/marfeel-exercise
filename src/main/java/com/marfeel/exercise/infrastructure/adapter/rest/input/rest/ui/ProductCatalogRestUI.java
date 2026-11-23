package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui;

import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductCatalogResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@Tag(name = "Catalog")
@ApiResponses({
	@ApiResponse(responseCode = "400", description = "Invalid request parameters, body or API version", content = @Content),
	@ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials", content = @Content),
	@ApiResponse(responseCode = "403", description = "The authenticated user cannot access this user", content = @Content)
})
@RequestMapping(value = "users", version = "v1")
public interface ProductCatalogRestUI {

	@Operation(summary = "List available products with current S3 metrics")
	@ApiResponse(responseCode = "200", description = "Available products from S1 and S2", useReturnTypeSchema = true)
	@GetMapping("{userId}/catalog")
	Mono<ProductCatalogResponse> catalog(@PathVariable("userId") UserType userId);
}
