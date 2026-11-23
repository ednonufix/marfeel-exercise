package com.marfeel.exercise.infrastructure.config;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.config.ApiVersionConfigurer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

class RegistryOpenApiConfigurationTests {

	private RegistryOpenApiConfiguration configuration;

	@BeforeEach
	void setUp() {
		configuration = new RegistryOpenApiConfiguration();
	}

	@Test
	void replacesDuplicateVersionHeadersAndPreservesOtherParameters() {
		var query = new QueryParameter().name("service");
		var get = new Operation().parameters(new ArrayList<>(List.of(query,
			new HeaderParameter().name("API-Version"), new HeaderParameter().name("api-version"))));
		var post = new Operation();
		var api = new OpenAPI().path("/users/{userId}/subscriptions", new PathItem().get(get).post(post));

		configuration.registryApiVersionHeader().customise(api);

		assertThat(get.getParameters()).hasSize(2).contains(query);
		assertThat(post.getParameters()).hasSize(1);
		for (var header : List.of(get.getParameters().get(1), post.getParameters().getFirst())) {
			assertThat(header.getName()).isEqualTo("API-Version");
			assertThat(header.getIn()).isEqualTo("header");
			assertThat(header.getRequired()).isTrue();
			assertThat(header.getSchema().getDefault()).isEqualTo("v1");
		}
	}

	@Test
	void removesCircularSchemaReferencesAndKeepsOtherCompositionEntries() {
		var other = new Schema<>().$ref("#/components/schemas/Other");
		var request = new ComposedSchema().addAllOfItem(new Schema<>().$ref("#/components/schemas/ProductRefRequest"))
			.addAllOfItem(other);
		var response = new ComposedSchema().addAllOfItem(new Schema<>().$ref("#/components/schemas/ProductRefResponse"));
		var plain = new Schema<>();
		var api = new OpenAPI().components(new Components().addSchemas("S1Request", request)
			.addSchemas("S2Response", response).addSchemas("S1Response", plain));

		configuration.productReferenceSchemas().customise(api);

		assertThat(request.getAllOf()).containsExactly(other);
		assertThat(response.getAllOf()).isNull();
		assertThat(plain.getAllOf()).isNull();
	}

	@Test
	void defaultsOnlyDocumentationPathsToV1() {
		var configurer = new ApiVersionConfigurer() {
			Mono<String> versionFor(String path) {
				return getApiVersionStrategy().resolveApiVersion(MockServerWebExchange.from(MockServerHttpRequest.get(path)));
			}
		};
		configuration.configureApiVersioning(configurer);

		for (String path : List.of("/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs.yaml",
				"/openapi/downstream.json")) {
			StepVerifier.create(configurer.versionFor(path)).expectNext("v1")
				.expectComplete().verify(Duration.ofSeconds(5));
		}
		StepVerifier.create(configurer.versionFor("/users/U1/subscriptions"))
			.expectComplete().verify(Duration.ofSeconds(5));
	}
}
