package com.marfeel.exercise.infrastructure.adapter.persistence.repository;

import java.time.Duration;

import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.persistence.mapper.SubscriptionPersistenceMapperImpl;
import com.marfeel.exercise.support.RegistryTestDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@Import({ProductSubscriptionAdapter.class, SubscriptionPersistenceMapperImpl.class})
class ProductSubscriptionAdapterTests {

	private @Autowired DatabaseClient database;
	private @Autowired ProductSubscriptionAdapter repository;
	private @Autowired ReactiveTransactionManager transactions;

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry properties) {
		RegistryTestDatabase.register(properties);
	}

	@Test
	void productFiltersDistinguishIdenticalCodesAcrossServices() {
		var seed = database.sql("""
		                        INSERT INTO subscriptions (user_id, service, product_code, product_subcode, provider_subscription_id)
		                        VALUES ('U3', 'S1', 'SHARED', 'X1', 'first'), ('U3', 'S2', 'SHARED', '', 'second')
		                        """).fetch().rowsUpdated();
		var byCode = repository.findActiveSubscriptions(UserType.U3, new CriteriaFilter(null, "SHARED", null, null));
		var byItem = repository.findActiveSubscriptions(UserType.U3, new CriteriaFilter(null, null, null, "SHARED"));
		TransactionalOperator.create(transactions).execute(tx -> {
					tx.setRollbackOnly();
					return seed.thenMany(Flux.concat(byCode, byItem)).map(Subscription::ref);
				}).as(StepVerifier::create)
				.expectNext(new S1Product("S1", "SHARED", "X1"), new S2Product("S2", "SHARED"))
				.expectComplete().verify(Duration.ofSeconds(10));
	}
}
