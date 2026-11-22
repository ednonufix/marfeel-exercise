package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui;

import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductCatalogResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping(value = "users", version = "v1")
public interface ProductCatalogRestUI {

	@GetMapping("{userId}/catalog")
	Mono<ProductCatalogResponse> catalog(@PathVariable("userId") UserType userId);
}
