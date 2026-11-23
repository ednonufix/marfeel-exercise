package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S3Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.CatalogCallerMapper;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S3ProductMetricsCallerTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private @Mock S3Client client;
	private S3ProductMetricsCaller caller;

	@BeforeEach
	void setUp() {
		caller = new S3ProductMetricsCaller(client, Mappers.getMapper(CatalogCallerMapper.class));
	}

	@Test
	void requestsS1MetricsUsingBothPartsOfTheProductIdentifier() {
		var product = new S1Product("S1", "A", "X2");
		given(client.getMetrics("S1", "A", "X2")).willReturn(Mono.just(new S1MetricResponse("S1", "A", "X2", 42, "ES01")));

		caller.getMetrics(product).as(StepVerifier::create)
			.expectNext(new ProductDetail(product, 42, "ES01")).expectComplete().verify(TIMEOUT);
	}

	@Test
	void requestsS2MetricsUsingTheItemIdentifier() {
		var product = new S2Product("S2", "item-7");
		given(client.getMetrics("S2", "item-7")).willReturn(Mono.just(new S2MetricResponse("S2", "item-7", 8, "ES02")));

		caller.getMetrics(product).as(StepVerifier::create)
			.expectNext(new ProductDetail(product, 8, "ES02")).expectComplete().verify(TIMEOUT);
	}
}
