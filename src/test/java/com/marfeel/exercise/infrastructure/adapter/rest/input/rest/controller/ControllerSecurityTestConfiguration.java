package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.controller;

import com.marfeel.exercise.infrastructure.config.SecurityConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@TestConfiguration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@Import(SecurityConfiguration.class)
class ControllerSecurityTestConfiguration {
}
