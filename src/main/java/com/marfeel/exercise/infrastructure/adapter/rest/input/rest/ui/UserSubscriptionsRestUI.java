package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui;

import java.util.List;

import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.CriteriaActiveProductRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.ProductSubscriptionRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductSubscriptionResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.UserSubscriptionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Subscriptions")
@ApiResponses({
		@ApiResponse(responseCode = "400", description = "Invalid request parameters, body or API version", content = @Content),
		@ApiResponse(responseCode = "401", description = "Missing or invalid Basic credentials", content = @Content),
		@ApiResponse(responseCode = "403", description = "The authenticated user cannot access this user", content = @Content)
})
@RequestMapping(value = "users", version = "v1")
public interface UserSubscriptionsRestUI {

	@Operation(summary = "List active subscriptions and attempt history")
	@ApiResponse(responseCode = "200", description = "Product filters apply only to active subscriptions", useReturnTypeSchema =
			true)
	@GetMapping("{userId}/subscriptions")
	Mono<UserSubscriptionsResponse> getSubscriptions(@PathVariable("userId") UserType userId,
			@ModelAttribute @ParameterObject CriteriaActiveProductRequest request);

	@Operation(summary = "Get one active subscription")
	@ApiResponse(responseCode = "200", description = "Active subscription with current metrics", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "No active subscription with this ID for this user", content = @Content)
	@GetMapping("{userId}/subscriptions/{subscriptionId}")
	Mono<UserSubscriptionsResponse.Active> getSubscription(@PathVariable("userId") UserType userId,
			@Parameter(description = "Local subscription ID from the active list", example = "1") @PathVariable("subscriptionId") long subscriptionId);

	@Operation(summary = "Unsubscribe from a product")
	@ApiResponse(responseCode = "200", description = "Remaining active subscriptions with metrics; an empty array after the last" +
	                                                 " removal", useReturnTypeSchema = true)
	@DeleteMapping("{userId}/subscriptions/{subscriptionId}")
	Mono<List<UserSubscriptionsResponse.Active>> unsubscribe(@PathVariable("userId") UserType userId,
			@Parameter(description = "Local subscription ID from the active list", example = "1") @PathVariable("subscriptionId") long subscriptionId);

	@Operation(summary = "Subscribe to specific products")
	@ApiResponse(responseCode = "200", description = "One outcome per distinct product. Match by ref; results may arrive in any " +
	                                                 "order. A provider rejection is returned as FAILED.", useReturnTypeSchema =
			true)
	@PostMapping("{userId}/subscriptions")
	Mono<ProductSubscriptionResponse> subscribe(@PathVariable("userId") UserType userId,
			@Valid @RequestBody ProductSubscriptionRequest request);

	@Operation(summary = "Search subscription attempts")
	@ApiResponse(responseCode = "200", description = "Attempts ordered newest first, including failed products",
			useReturnTypeSchema = true)
	@GetMapping("{userId}/subscription-attempts")
	Flux<UserSubscriptionsResponse.Attempt> attempts(@PathVariable("userId") UserType userId,
			@Parameter(description = "Persisted attempt outcome (SUCCESS or FAILED)") @RequestParam(required = false) SubscriptionStatus status,
			@Parameter(description = "Provider identifier", example = "S2") @RequestParam(required = false) String service);
}
