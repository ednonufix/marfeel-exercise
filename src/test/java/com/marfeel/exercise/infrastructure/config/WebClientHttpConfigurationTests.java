package com.marfeel.exercise.infrastructure.config;

import com.marfeel.exercise.infrastructure.adapter.rest.output.rest.client.A1Client;
import com.marfeel.exercise.infrastructure.adapter.rest.output.token.Token;
import com.marfeel.exercise.infrastructure.adapter.rest.output.token.TokenResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroupConfigurer.ClientCallback;
import org.springframework.web.service.registry.HttpServiceGroupConfigurer.Groups;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WebClientHttpConfigurationTests {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private WebClientHttpConfiguration configuration;

	@BeforeEach
	void setUp() {
		configuration = new WebClientHttpConfiguration();
	}

	@Test
	void configuresTheA1BaseUrlWithoutAnAuthorizationHeader() {
		var request = requestFor("A1", configuration.a1("https://a1.example"));
		assertThat(request.url()).hasToString("https://a1.example/probe");
		assertThat(request.headers().getFirst("Authorization")).isNull();
	}

	@Test
	void configuresTheS1BaseUrlAndUsesTheA1Token() {
		var request = requestFor("S1", configuration.s1("https://s1.example", () -> Mono.just(() -> "session-token")));
		assertThat(request.url()).hasToString("https://s1.example/probe");
		assertThat(request.headers().getFirst("Authorization")).isEqualTo("Bearer session-token");
	}

	@Test
	void configuresTheS2BaseUrlAndApiKey() {
		var request = requestFor("S2", configuration.s2("https://s2.example", "api-key"));
		assertThat(request.url()).hasToString("https://s2.example/probe");
		assertThat(request.headers().getFirst("X-Api-Key")).isEqualTo("api-key");
	}

	@Test
	void configuresTheS3BaseUrlAndBearerToken() {
		var request = requestFor("S3", configuration.s3("https://s3.example", "metrics-token"));
		assertThat(request.url()).hasToString("https://s3.example/probe");
		assertThat(request.headers().getFirst("Authorization")).isEqualTo("Bearer metrics-token");
	}

	@Test
	void createsATokenProviderThatFetchesLazilyAndReusesTheToken() {
		var client = mock(A1Client.class);
		given(client.fetchToken()).willReturn(Mono.just(new TokenResponse("session-token", 120)));
		var provider = configuration.a1TokenProvider(client);

		verifyNoInteractions(client);
		provider.getToken().concatWith(provider.getToken()).map(Token::token).as(StepVerifier::create)
			.expectNext("session-token", "session-token").expectComplete().verify(TIMEOUT);
		then(client).should().fetchToken();
	}

	@SuppressWarnings("unchecked")
	private static ClientRequest requestFor(String group, WebClientHttpServiceGroupConfigurer configurer) {
		var builder = WebClient.builder();
		Groups<WebClient.Builder> groups = mock(Groups.class);
		given(groups.filterByName(group)).willReturn(groups);
		willAnswer(call -> {
			call.<ClientCallback<WebClient.Builder>>getArgument(0).withClient(null, builder);
			return null;
		}).given(groups).forEachClient(any(ClientCallback.class));

		configurer.configureGroups(groups);
		var sent = new AtomicReference<ClientRequest>();
		builder.exchangeFunction(request -> {
			sent.set(request);
			return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
		}).build().get().uri("/probe").retrieve().toBodilessEntity().as(StepVerifier::create)
			.expectNextCount(1).expectComplete().verify(TIMEOUT);
		return sent.get();
	}
}
