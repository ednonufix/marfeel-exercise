package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.caller;

import com.marfeel.exercise.application.ports.output.ProductMetricsClient;
import com.marfeel.exercise.domain.model.ProductDetail;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S3Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.mapper.CatalogCallerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class S3ProductMetricsCaller implements ProductMetricsClient {

	private final S3Client s3Client;
	private final CatalogCallerMapper mapper;

	@Override
	public Mono<ProductDetail> getMetrics(ProductRef ref) {
		return switch (ref) {
			case S1Product product -> s3Client.getMetrics(product.service(), product.code(), product.subcode()).map(mapper::toDomain);
			case S2Product product -> s3Client.getMetrics(product.service(), product.itemId()).map(mapper::toDomain);
		};
	}
}
