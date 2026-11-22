package com.marfeel.exercise;

import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.marfeel.exercise.application.dto.CriteriaFilter;
import com.marfeel.exercise.application.dto.Subscription;
import com.marfeel.exercise.application.ports.output.UserSubscriptionsRepository;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.mysql.MySQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"endpoint.s2.key=test-api-key", "endpoint.s3.token=test-metrics-token"})
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
		"endpoint.a1.url", "endpoint.s1.url", "endpoint.s2.url", "endpoint.s3.url"}))
class MarfeelApplicationTests {
	@LocalServerPort
	int port;
	private @InjectWireMock WireMockServer wireMock;

	@DynamicPropertySource
	static void database(DynamicPropertyRegistry properties) {
		var port = System.getProperty("registry.test.mysql.port");
		var password = System.getenv("REGISTRY_TEST_MYSQL_PASSWORD");
		if (port == null) {
			var mysql = new MySQLContainer("mysql:8").withDatabaseName("schema_check")
					.withUsername("verify_user").withPassword("test-password");
			mysql.start();
			port = mysql.getMappedPort(3306).toString();
			configureDatabase(properties, mysql.getHost(), port, mysql.getPassword());
		} else {
			configureDatabase(properties, "127.0.0.1", port, password);
		}
	}

	private static void configureDatabase(DynamicPropertyRegistry p, String host, String port, String password) {
		var address = host + ":" + port + "/schema_check";
		p.add("spring.r2dbc.url", () -> "r2dbc:mysql://" + address);
		p.add("spring.liquibase.url", () -> "jdbc:mysql://" + address);
		p.add("spring.liquibase.default-schema", () -> "schema_check");
		p.add("spring.r2dbc.username", () -> "verify_user");
		p.add("spring.liquibase.user", () -> "verify_user");
		p.add("spring.r2dbc.password", () -> password);
		p.add("spring.liquibase.password", () -> password);
	}

	private WebTestClient client(String user) {
		return WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/users")
				.defaultHeader("API-Version", "v1")
				.defaultHeaders(headers -> headers.setBasicAuth(user, user)).build();
	}

	@Test
	void subscriptionsRecordEachProviderOutcomeAndKeepFailedProductsAvailable() {
		assertDoesNotThrow(() -> {
			String batch = """
			               {"products":[{"service":"S1","code":"S1-A","subcode":"X1"},{"service":"S2","itemId":"S2-100"}]}
			               """;
			for (var user : List.of("U1", "U2")) {
				var http = client(user);
				boolean u1 = "U1".equals(user);
				String failedService = u1 ? "S2" : "S1";
				http.post().uri("/" + user + "/subscriptions").contentType(APPLICATION_JSON).bodyValue(batch)
						.exchange().expectStatus().isOk().expectBody()
						.jsonPath("$.results[0].status").isEqualTo(u1 ? "SUCCESS" : "FAILED")
						.jsonPath("$.results[1].status").isEqualTo(u1 ? "FAILED" : "SUCCESS");
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
			client("U1").get().uri("/U2/subscriptions").exchange().expectStatus().isForbidden();
		});
	}

	@Test
	void repeatedSubscriptionSkipsProviderUntilTheProductIsDeleted() {
		String subscription = """
		                      {"products":[{"service":"S1","code":"S1-A","subcode":"X1"}]}
		                      """;
		var web = WebClient.builder().baseUrl("http://localhost:" + port + "/users/U3")
				.defaultHeaders(h -> h.setBasicAuth("U3", "U3")).defaultHeader("API-Version", "v1").build();
		var post = web.post().uri("/subscriptions").contentType(APPLICATION_JSON).bodyValue(subscription)
				.retrieve().bodyToMono(JsonNode.class).map(body -> body.at("/results/0/status").asString());

		post.as(StepVerifier::create).expectNext("SUCCESS").verifyComplete();
		post.as(StepVerifier::create).expectNext("ALREADY_SUBSCRIBED").verifyComplete();
		wireMock.verify(1, postRequestedFor(urlPathEqualTo("/s1/subscribe"))
				.withRequestBody(matchingJsonPath("$.account.ref", equalTo("U3"))));
		var user = client("U3");
		user.get().uri("/U3/subscriptions?itemId=S1-A").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.activeSubscription.length()").isEqualTo(0);
		var id = web.get().uri("/subscriptions").retrieve().bodyToMono(JsonNode.class)
				.map(body -> body.at("/activeSubscription/0/id").asLong());
		id.flatMap(value -> web.delete().uri("/subscriptions/" + value).retrieve().bodyToMono(JsonNode.class))
				.as(StepVerifier::create).assertNext(list -> assertThat(list.size()).isZero()).verifyComplete();
		post.as(StepVerifier::create).expectNext("SUCCESS").verifyComplete();
		user.get().uri("/U3/subscription-attempts?status=SUCCESS").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$.length()").isEqualTo(2);
	}


	@Test
	void productFiltersDistinguishIdenticalCodesAcrossServices(@Autowired DatabaseClient db,
			@Autowired UserSubscriptionsRepository repository, @Autowired ReactiveTransactionManager transactions) {
		var seed = db.sql("""
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
				.verifyComplete();
	}

}
