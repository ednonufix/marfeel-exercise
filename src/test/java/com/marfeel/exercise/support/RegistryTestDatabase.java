package com.marfeel.exercise.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class RegistryTestDatabase {

	private static final String DATABASE = "schema_check";

	@Bean
	@ServiceConnection
	MySQLContainer registryMysql() {
		return new MySQLContainer("mysql:8").withDatabaseName(DATABASE);
	}

	@Bean
	DynamicPropertyRegistrar registryTestLiquibaseSchema() {
		return properties -> properties.add("spring.liquibase.default-schema", () -> DATABASE);
	}
}
