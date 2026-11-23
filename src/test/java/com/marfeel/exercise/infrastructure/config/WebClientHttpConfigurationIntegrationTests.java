package com.marfeel.exercise.infrastructure.config;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.A1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S3Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S1ProductsResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2ItemsResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto.S2MetricResponse;
import com.marfeel.exercise.infrastructure.adapter.rest.output.token.TokenResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webclient.autoconfigure.service.ReactiveHttpServiceClientAutoConfiguration;

import java.time.Duration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import reactor.test.StepVerifier;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
	classes = WebClientHttpConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {"endpoint.s2.key=test-api-key", "endpoint.s3.token=test-metrics-token"})
@ImportAutoConfiguration({WebClientAutoConfiguration.class, HttpServiceClientPropertiesAutoConfiguration.class,
	ReactiveHttpServiceClientAutoConfiguration.class})
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
	"endpoint.a1.url", "endpoint.s1.url", "endpoint.s2.url", "endpoint.s3.url"}))
class WebClientHttpConfigurationIntegrationTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final String TOKEN_PATH = "/a1/token";
	private @InjectWireMock WireMockServer wireMock;
	private @Autowired A1Client a1;
	private @Autowired S1Client s1;
	private @Autowired S2Client s2;
	private @Autowired S3Client s3;

	@Test
	void a1IssuesToken() {
		a1.fetchToken().as(StepVerifier::create)
			.expectNext(new TokenResponse("catalog-token", 300))
			.expectComplete()
			.verify(TIMEOUT);
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(TOKEN_PATH)).withHeader("Authorization", absent()));
	}

	@Test
	void s1LoadsProductsUsingCachedA1Token() {
		s1.getProducts().as(StepVerifier::create)
			.assertNext(response -> assertThat(response.products())
				.extracting(S1ProductsResponse.ProductEntry::product)
				.containsExactly(new S1ProductsResponse.Product("S1-A", "X1")))
			.expectComplete()
			.verify(TIMEOUT);
		s1.getProducts().as(StepVerifier::create)
			.assertNext(response -> assertThat(response.products()).hasSize(1))
			.expectComplete()
			.verify(TIMEOUT);

		wireMock.verify(1, postRequestedFor(urlPathEqualTo(TOKEN_PATH)));
		wireMock.verify(2, getRequestedFor(urlPathEqualTo("/s1/products"))
			.withHeader("Authorization", equalTo("Bearer catalog-token")));
	}

	@Test
	void s2LoadsItemsUsingApiKey() {
		s2.getItems().as(StepVerifier::create)
			.assertNext(response -> assertThat(response.items())
				.extracting(S2ItemsResponse.ItemEntry::item)
				.containsExactly(new S2ItemsResponse.Item("S2-100")))
			.expectComplete()
			.verify(TIMEOUT);
	}

	@Test
	void s3LoadsMetricsForBothProductIdentifiers() {
		s3.getMetrics("S1", "S1-A", "X1").as(StepVerifier::create)
			.expectNext(new S1MetricResponse("S1", "S1-A", "X1", 12, "test-iban-s1"))
			.expectComplete()
			.verify(TIMEOUT);
		s3.getMetrics("S2", "S2-100").as(StepVerifier::create)
			.expectNext(new S2MetricResponse("S2", "S2-100", 34, "test-iban-s2"))
			.expectComplete()
			.verify(TIMEOUT);
	}
}
