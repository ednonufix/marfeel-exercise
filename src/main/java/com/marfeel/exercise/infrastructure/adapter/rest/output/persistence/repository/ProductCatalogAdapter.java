package com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.repository;

import com.marfeel.exercise.application.ports.output.ProductCatalogRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.entity.SubscriptionEntity;
import com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.mapper.SubscriptionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
@Repository
public class ProductCatalogAdapter implements ProductCatalogRepository {

	private final R2dbcEntityTemplate entityTemplate;
	private final SubscriptionPersistenceMapper mapper;

	@Override
	public Flux<ProductRef> findActiveProductRefs(UserType userType) {
		return entityTemplate.select(SubscriptionEntity.class)
				.matching(query(where("userId").is(userType.name())))
				.all()
				.map(mapper::toProductRef);
	}
}
