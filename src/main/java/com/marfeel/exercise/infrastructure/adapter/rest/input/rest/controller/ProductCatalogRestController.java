package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.controller;

import com.marfeel.exercise.application.ports.input.ProductCatalogUseCase;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.mapper.ProductCatalogMapper;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductCatalogResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.ui.ProductCatalogRestUI;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class ProductCatalogRestController implements ProductCatalogRestUI {

	private final ProductCatalogMapper mapper;
	private final ProductCatalogUseCase catalogUseCase;

	@Override
	public Mono<ProductCatalogResponse> catalog(UserType userId) {
		return catalogUseCase.allProducts(userId).map(mapper::fromDomain);
	}
}
