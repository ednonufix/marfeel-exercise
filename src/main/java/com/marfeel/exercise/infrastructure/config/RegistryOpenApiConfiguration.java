package com.marfeel.exercise.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ApiVersionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(title = "Subscription Registry", version = "v1",
	description = "Manage a user's product subscriptions and inspect provider outcomes."),
	security = @SecurityRequirement(name = "registryBasic"))
@SecurityScheme(name = "registryBasic", type = SecuritySchemeType.HTTP, scheme = "basic")
public class RegistryOpenApiConfiguration implements WebFluxConfigurer {

	static String[] documentationPaths() {
		return new String[]{"/swagger-ui.html", "/swagger-ui/**", "/webjars/swagger-ui/**",
			"/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml", "/openapi/downstream.json"};
	}

	@Override
	public void configureApiVersioning(ApiVersionConfigurer configurer) {
		var patterns = Arrays.stream(documentationPaths()).map(PathPatternParser.defaultInstance::parse).toList();
		configurer.useVersionResolver(exchange -> {
			var path = exchange.getRequest().getPath().pathWithinApplication();
			return patterns.stream().anyMatch(pattern -> pattern.matches(path)) ? "v1" : null;
		});
	}


	@Bean
	OpenApiCustomizer registryApiVersionHeader() {
		return api -> api.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
			if (operation.getParameters() != null) {
				operation.getParameters().removeIf(parameter -> "API-Version".equalsIgnoreCase(parameter.getName()));
			}
			operation.addParametersItem(new HeaderParameter().name("API-Version").required(true)
				.description("Registry API version").schema(new StringSchema()._enum(List.of("v1"))._default("v1")));
		}));
	}

	@Bean
	OpenApiCustomizer productReferenceSchemas() {
		return api -> {
			var schemas = api.getComponents().getSchemas();
			for (var suffix : List.of("Request", "Response")) {
				var base = "#/components/schemas/ProductRef" + suffix;
				for (var service : List.of("S1", "S2")) {
					var subtype = schemas.get(service + suffix);
					if (subtype != null && subtype.getAllOf() != null) {
						subtype.getAllOf().removeIf(part -> part instanceof Schema<?> schema && base.equals(schema.get$ref()));
						if (subtype.getAllOf().isEmpty()) {
							subtype.setAllOf(null);
						}
					}
				}
			}
		};
	}
}
