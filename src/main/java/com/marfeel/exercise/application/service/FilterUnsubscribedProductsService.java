package com.marfeel.exercise.application.service;

import com.marfeel.exercise.application.ports.input.FilterUnsubscribedProductsUseCase;
import com.marfeel.exercise.application.ports.output.ProductCatalogRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FilterUnsubscribedProductsService implements FilterUnsubscribedProductsUseCase {

	private final ProductCatalogRepository catalogRepository;

	@Override
	public Flux<ProductRef> filter(UserType userType, Flux<ProductRef> products) {
		return catalogRepository.findActiveProductRefs(userType)
			.collect(Collectors.toSet())
			.flatMapMany(activeRefs -> products.filter(Predicate.not(activeRefs::contains)));
	}
}
