package com.marfeel.exercise;

import com.marfeel.exercise.support.RegistryTestDatabase;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Duration;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@Import(RegistryTestDatabase.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {"endpoint.s2.key=test-api-key", "endpoint.s3.token=test-metrics-token"})
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
	"endpoint.a1.url", "endpoint.s1.url", "endpoint.s2.url", "endpoint.s3.url"}))
class MarfeelApplicationTests {
	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private @LocalServerPort int port;
	private @InjectWireMock WireMockServer wireMock;

	private WebTestClient client(String user) {
		return WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/users")
			.defaultHeader("API-Version", "v1")
			.defaultHeaders(headers -> headers.setBasicAuth(user, user)).build();
	}

	@Test
	void subscriptionsRecordEachProviderOutcomeAndKeepFailedProductsAvailable() {
		String batch = """
			{"products":[{"service":"S1","code":"S1-A","subcode":"X1"},{"service":"S2","itemId":"S2-100"}]}
			""";
		for (var user : List.of("U1", "U2")) {
			var http = client(user);
			boolean u1 = "U1".equals(user);
			String failedService = u1 ? "S2" : "S1";
			http.post().uri("/" + user + "/subscriptions").contentType(APPLICATION_JSON).bodyValue(batch)
				.exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.results[?(@.ref.service == 'S1')].status").isEqualTo(List.of(u1 ? "SUCCESS" : "FAILED"))
				.jsonPath("$.results[?(@.ref.service == 'S2')].status").isEqualTo(List.of(u1 ? "FAILED" : "SUCCESS"));
			var failures = http.get().uri("/" + user + "/subscription-attempts?status=FAILED&service=" + failedService)
				.exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(1)
				.jsonPath("$[0].ref.service").isEqualTo(failedService);
			if (u1) {
				failures.jsonPath("$[0].failureReason").isEqualTo("user not eligible");
			}
			http.get().uri("/" + user + "/catalog").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.products.length()").isEqualTo(1).jsonPath("$.products[0].ref.service").isEqualTo(failedService);
			http.get().uri("/" + user + "/subscriptions").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.activeSubscription.length()").isEqualTo(1)
				.jsonPath("$.activeSubscription[0].detail.iban").exists()
				.jsonPath("$.attemptsSubscription.length()").isEqualTo(2);
		}
		WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build()
			.get().uri("/users/U1/subscriptions").header("API-Version", "v1").exchange().expectStatus().isUnauthorized();
		client("U1").post().uri("/U1/subscriptions").contentType(APPLICATION_JSON)
			.bodyValue("""
				{"products":[{"service":"S2","itemId":"S2-100"}]}
				""")
			.exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.results[0].status").isEqualTo("FAILED");
		wireMock.verify(2, postRequestedFor(urlPathEqualTo("/s2/subscribe"))
			.withRequestBody(matchingJsonPath("$.user", equalTo("U1"))));
		client("U1").get().uri("/U2/subscriptions").exchange().expectStatus().isForbidden();
	}

	@Test
	void concurrentSubscriptionsInvokeProviderOnceUntilDeleted() {
		String subscription = """
			{"products":[{"service":"S1","code":"S1-A","subcode":"X1"}]}
			""";
		var web = WebClient.builder().baseUrl("http://localhost:" + port + "/users/U3")
			.defaultHeaders(h -> h.setBasicAuth("U3", "U3")).defaultHeader("API-Version", "v1").build();
		var post = web.post().uri("/subscriptions").contentType(APPLICATION_JSON).bodyValue(subscription)
			.retrieve().bodyToMono(JsonNode.class).map(body -> body.at("/results/0/status").asString());

		Mono.zip(post, post).as(StepVerifier::create)
			.assertNext(pair -> assertThat(List.of(pair.getT1(), pair.getT2())).containsExactlyInAnyOrder("SUCCESS",
				"IN_PROGRESS"))
			.expectComplete().verify(TIMEOUT);
		post.as(StepVerifier::create).expectNext("ALREADY_SUBSCRIBED").expectComplete().verify(TIMEOUT);
		wireMock.verify(1, postRequestedFor(urlPathEqualTo("/s1/subscribe"))
			.withRequestBody(matchingJsonPath("$.account.ref", equalTo("U3"))));
		var user = client("U3");
		user.get().uri("/U3/subscriptions?itemId=S1-A").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.activeSubscription.length()").isEqualTo(0);
		var active = user.get().uri("/U3/subscriptions?service=S1&subcode=X1").exchange()
			.expectStatus().isOk().expectBody(JsonNode.class).returnResult().getResponseBody();
		assertThat(active).isNotNull();
		long id = active.at("/activeSubscription/0/id").asLong();
		assertThat(id).isPositive();
		user.get().uri("/U3/subscriptions/" + id).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.id").isEqualTo(id)
			.jsonPath("$.detail.ref.code").isEqualTo("S1-A")
			.jsonPath("$.detail.iban").exists();
		client("U2").get().uri("/U2/subscriptions/" + id).exchange().expectStatus().isNotFound();
		user.delete().uri("/U3/subscriptions/" + id).exchange().expectStatus().isOk()
			.expectBodyList(JsonNode.class).hasSize(0);
		user.get().uri("/U3/subscriptions/" + id).exchange().expectStatus().isNotFound();
		post.as(StepVerifier::create).expectNext("SUCCESS").expectComplete().verify(TIMEOUT);
		user.get().uri("/U3/subscription-attempts?status=SUCCESS").exchange().expectStatus().isOk()
			.expectBody().jsonPath("$.length()").isEqualTo(2);
	}


	@Test
	void publishesRegistryAndProviderContracts() {
		var http = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		var location = http.get().uri("/swagger-ui.html").exchange().expectStatus().is3xxRedirection()
			.returnResult(Void.class).getResponseHeaders().getLocation();
		assertThat(location).isNotNull();
		http.get().uri(location.toString()).exchange().expectStatus().isOk();
		http.get().uri("/v3/api-docs/swagger-config").exchange().expectStatus().isOk();
		http.get().uri("/v3/api-docs.yaml").exchange().expectStatus().isOk();
		http.get().uri("/v3/api-docs").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.info.title").isEqualTo("Subscription Registry")
			.jsonPath("$.components.securitySchemes.registryBasic.scheme").isEqualTo("basic")
			.jsonPath("$.paths['/users/{userId}/subscriptions'].post.requestBody.content['application/json'].schema").exists()
			.jsonPath("$.paths['/users/{userId}/subscriptions'].post.responses['200'].content['application/json'].schema").exists()
			.jsonPath("$.paths['/users/{userId}/subscriptions'].post.parameters[?(@.name == 'API-Version')]").isNotEmpty()
			.jsonPath("$.paths['/users/{userId}/subscriptions/{subscriptionId}'].get.responses['404']").exists()
			.jsonPath("$.components.schemas.ProductRefRequest.discriminator.mapping.S2").exists()
			.jsonPath("$.components.schemas.ProductRefResponse.oneOf.length()").isEqualTo(2)
			.jsonPath("$.components.schemas.S1Request.allOf[*]['$ref']").isEmpty()
			.jsonPath("$.components.schemas.S2Request.allOf[*]['$ref']").isEmpty()
			.jsonPath("$.components.schemas.S1Response.allOf[*]['$ref']").isEmpty()
			.jsonPath("$.components.schemas.S2Response.allOf[*]['$ref']").isEmpty();
		http.get().uri("/openapi/downstream.json").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.paths['/s2/subscribe'].post.requestBody").exists()
			.jsonPath("$.components.schemas.S2Result.oneOf.length()").isEqualTo(2);
	}
}
