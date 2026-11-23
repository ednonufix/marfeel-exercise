package com.marfeel.exercise.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.mysql.MySQLContainer;

public final class RegistryTestDatabase {

	private static final String DATABASE = "schema_check";
	private static final String USER = "verify_user";

	private RegistryTestDatabase() {
	}

	public static void register(DynamicPropertyRegistry properties) {
		var port = System.getProperty("registry.test.mysql.port");
		if (port != null) {
			configure(properties, "127.0.0.1", port, System.getenv("REGISTRY_TEST_MYSQL_PASSWORD"));
			return;
		}
		var mysql = ContainerHolder.MYSQL;
		configure(properties, mysql.getHost(), mysql.getMappedPort(3306).toString(), mysql.getPassword());
	}

	private static void configure(DynamicPropertyRegistry properties, String host, String port, String password) {
		var address = host + ":" + port + "/" + DATABASE;
		properties.add("spring.r2dbc.url", () -> "r2dbc:mysql://" + address);
		properties.add("spring.liquibase.url", () -> "jdbc:mysql://" + address);
		properties.add("spring.liquibase.default-schema", () -> DATABASE);
		properties.add("spring.r2dbc.username", () -> USER);
		properties.add("spring.liquibase.user", () -> USER);
		properties.add("spring.r2dbc.password", () -> password);
		properties.add("spring.liquibase.password", () -> password);
	}

	private static class ContainerHolder {
		private static final MySQLContainer MYSQL = start();

		private static MySQLContainer start() {
			var mysql = new MySQLContainer("mysql:8").withDatabaseName(DATABASE)
					.withUsername(USER).withPassword("test-password");
			mysql.start();
			return mysql;
		}
	}
}
