package com.marfeel.exercise.infrastructure.adapter.persistence.repository;

import com.marfeel.exercise.application.dto.Attempt;
import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.application.ports.output.ProductSubscriptionRepository;
import com.marfeel.exercise.application.ports.output.UserSubscriptionsRepository;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.SubscriptionStatus;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.persistence.entity.SubscriptionAttemptEntity;
import com.marfeel.exercise.infrastructure.adapter.persistence.entity.SubscriptionEntity;
import com.marfeel.exercise.infrastructure.adapter.persistence.entity.SubscriptionKey;
import com.marfeel.exercise.infrastructure.adapter.persistence.mapper.SubscriptionPersistenceMapper;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ProductSubscriptionAdapter implements ProductSubscriptionRepository,
	UserSubscriptionsRepository {

	private final R2dbcEntityTemplate entityTemplate;
	private final SubscriptionPersistenceMapper mapper;

	@Override
	public Mono<Boolean> isActive(UserType user, ProductRef product) {
		var key = SubscriptionKey.of(user, product);
		return entityTemplate.select(SubscriptionEntity.class).matching(query(key.criteria())).exists();
	}

	@Override
	@Transactional
	public Mono<Void> save(SaveSubscriptionCommand command) {
		var attempt = mapper.toAttempt(command);
		Mono<Void> saveOutcome = switch (command.status()) {
			case SUCCESS -> entityTemplate.insert(mapper.toSubscription(attempt, command.providerSubscriptionId())).then();
			case FAILED -> Mono.empty();
			default -> Mono.error(new IllegalArgumentException("Expected a provider outcome"));
		};
		return saveOutcome.then(entityTemplate.insert(attempt)).then();
	}

	@Override
	public Flux<Subscription> findActiveSubscriptions(UserType userType, CriteriaFilter filter) {
		return entityTemplate.select(SubscriptionEntity.class)
			.matching(query(buildActiveSubscriptionCriteria(userType, filter))
				.sort(Sort.by(Sort.Direction.DESC, "subscribedAt", "id")))
			.all()
			.map(mapper::toSubscription);
	}

	@Override
	public Mono<Subscription> findActiveSubscription(UserType userType, long subscriptionId) {
		return entityTemplate.select(SubscriptionEntity.class)
			.matching(query(where("id").is(subscriptionId).and("userId").is(userType.name())))
			.one()
			.map(mapper::toSubscription);
	}

	@Override
	@Transactional
	public Mono<Void> deleteActiveSubscription(UserType userType, long subscriptionId) {
		var target = query(where("id").is(subscriptionId).and("userId").is(userType.name()));
		return entityTemplate.delete(SubscriptionEntity.class).matching(target).all().then();
	}

	@Override
	public Flux<Attempt> findAttempts(UserType userType, SubscriptionStatus status, String service) {
		var criteria = where("userId").is(userType.name());
		if (status != null) {
			criteria = criteria.and("subscriptionStatus").is(status.name());
		}
		if (StringUtils.hasText(service)) {
			criteria = criteria.and("service").is(service);
		}
		return entityTemplate.select(SubscriptionAttemptEntity.class)
			.matching(query(criteria).sort(Sort.by(Sort.Direction.DESC, "attemptedAt", "id")))
			.all()
			.map(mapper::toAttempt);
	}

	private static Criteria buildActiveSubscriptionCriteria(UserType userType, CriteriaFilter filter) {
		var criteria = where("userId").is(userType.name());
		if (filter == null) {
			return criteria;
		}
		if (StringUtils.hasText(filter.service())) {
			criteria = criteria.and("service").is(filter.service());
		}
		if (StringUtils.hasText(filter.code())) {
			criteria = criteria.and("service").is("S1").and("productCode").is(filter.code());
		}
		if (StringUtils.hasText(filter.subcode())) {
			criteria = criteria.and("service").is("S1").and("productSubcode").is(filter.subcode());
		}
		if (StringUtils.hasText(filter.itemId())) {
			criteria = criteria.and("service").is("S2").and("productCode").is(filter.itemId());
		}
		return criteria;
	}
}
