package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2MetricResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface S3Client {

	@GetExchange("/s3/metrics")
	Mono<S1MetricResponse> getMetrics(
			@RequestParam String service,
			@RequestParam String code,
			@RequestParam("subcode") String subCode
	);

	@GetExchange("/s3/metrics")
	Mono<S2MetricResponse> getMetrics(
			@RequestParam String service,
			@RequestParam String itemId
	);
}
