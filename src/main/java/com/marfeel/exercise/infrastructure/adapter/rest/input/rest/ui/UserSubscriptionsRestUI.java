package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui;

import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.CriteriaActiveProductRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.request.ProductSubscriptionRequest;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductSubscriptionResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.UserSubscriptionsResponse;
import jakarta.validation.Valid;

import java.util.List;

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

@RequestMapping(value = "users", version = "v1")
public interface UserSubscriptionsRestUI {

	@GetMapping("{userId}/subscriptions")
	Mono<UserSubscriptionsResponse> getSubscriptions(@PathVariable("userId") UserType userId,
			@ModelAttribute @ParameterObject CriteriaActiveProductRequest request);

	@GetMapping("{userId}/subscriptions/{subscriptionId}")
	Mono<UserSubscriptionsResponse.Active> getSubscription(@PathVariable("userId") UserType userId,
			@PathVariable("subscriptionId") long subscriptionId);

	@DeleteMapping("{userId}/subscriptions/{subscriptionId}")
	Mono<List<UserSubscriptionsResponse.Active>> unsubscribe(@PathVariable("userId") UserType userId,
			@PathVariable("subscriptionId") long subscriptionId);

	@PostMapping("{userId}/subscriptions")
	Mono<ProductSubscriptionResponse> subscribe(@PathVariable("userId") UserType userId,
			@Valid @RequestBody ProductSubscriptionRequest request);

	@GetMapping("{userId}/subscription-attempts")
	Flux<UserSubscriptionsResponse.Attempt> attempts(@PathVariable("userId") UserType userId,
			@RequestParam(required = false) SubscriptionStatus status, @RequestParam(required = false) String service);
}
