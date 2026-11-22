package com.marfeel.exercise.infrastructure.config;

import java.util.Map;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.A1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S2Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.S3Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.filter.AuthenticationFilterFunction;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.filter.CustomHeadersFilterFunction;
import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.filter.TokenFilterFunction;
import com.marfeel.exercise.infrastructure.adapter.rest.output.token.CachedTokenProvider;
import com.marfeel.exercise.infrastructure.adapter.rest.output.token.TokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup.ClientType;
import org.springframework.web.service.registry.ImportHttpServices;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = WebClientHttpConfiguration.A1_GROUP_NAME, types = A1Client.class, clientType = ClientType.WEB_CLIENT)
@ImportHttpServices(group = WebClientHttpConfiguration.S1_GROUP_NAME, types = S1Client.class, clientType = ClientType.WEB_CLIENT)
@ImportHttpServices(group = WebClientHttpConfiguration.S2_GROUP_NAME, types = S2Client.class, clientType = ClientType.WEB_CLIENT)
@ImportHttpServices(group = WebClientHttpConfiguration.S3_GROUP_NAME, types = S3Client.class, clientType = ClientType.WEB_CLIENT)
public class WebClientHttpConfiguration {

	static final String A1_GROUP_NAME = "A1";
	static final String S1_GROUP_NAME = "S1";
	static final String S2_GROUP_NAME = "S2";
	static final String S3_GROUP_NAME = "S3";

	@Bean
	WebClientHttpServiceGroupConfigurer a1(@Value("${endpoint.a1.url}") String url) {
		return groups -> groups.filterByName(A1_GROUP_NAME)
				.forEachClient((group, builder) -> builder.baseUrl(url));
	}

	@Bean
	TokenProvider a1TokenProvider(@Lazy A1Client client) {
		return new CachedTokenProvider(Mono.defer(client::fetchToken));
	}

	@Bean
	WebClientHttpServiceGroupConfigurer s1(@Value("${endpoint.s1.url}") String url,
			@Qualifier("a1TokenProvider") TokenProvider tokenProvider) {
		return groups -> groups.filterByName(S1_GROUP_NAME)
				.forEachClient((group, builder) -> builder.baseUrl(url)
						.filter(new TokenFilterFunction(tokenProvider)));
	}

	@Bean
	WebClientHttpServiceGroupConfigurer s2(@Value("${endpoint.s2.url}") String url, @Value("${endpoint.s2.key}") String key) {
		return groups -> groups.filterByName(S2_GROUP_NAME)
				.forEachClient((group, builder) -> builder.baseUrl(url)
						.filter(new CustomHeadersFilterFunction(MultiValueMap.fromSingleValue(Map.of("X-Api-Key", key)))));
	}

	@Bean
	WebClientHttpServiceGroupConfigurer s3(@Value("${endpoint.s3.url}") String url,
			@Value("${endpoint.s3.token}") String token) {
		return groups -> groups.filterByName(S3_GROUP_NAME)
				.forEachClient((group, builder) -> builder.baseUrl(url)
						.filter(new AuthenticationFilterFunction(token)));
	}

}
