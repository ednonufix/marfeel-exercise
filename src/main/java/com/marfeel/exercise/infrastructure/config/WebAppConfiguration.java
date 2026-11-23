package com.marfeel.exercise.infrastructure.config;

import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.converter.UserTypeConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebAppConfiguration implements WebFluxConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new UserTypeConverter());
	}
}
