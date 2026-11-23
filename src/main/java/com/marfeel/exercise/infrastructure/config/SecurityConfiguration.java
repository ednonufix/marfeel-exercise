package com.marfeel.exercise.infrastructure.config;

import java.util.Arrays;

import com.marfeel.exercise.domain.vo.UserType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	MapReactiveUserDetailsService registryUsers() {
		var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		var users = Arrays.stream(UserType.values()).map(user -> User.withUsername(user.name())
				.password(encoder.encode(user.name())).roles("USER").build()).toList();
		return new MapReactiveUserDetailsService(users);
	}

	@Bean
	SecurityWebFilterChain registrySecurity(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.httpBasic(Customizer.withDefaults())
				.authorizeExchange(routes -> routes
						.pathMatchers(RegistryOpenApiConfiguration.documentationPaths()).permitAll()
						.pathMatchers("/users/{userId}/**")
						.access((auth, context) -> auth.map(principal ->
										principal.getName().equalsIgnoreCase(String.valueOf(context.getVariables().get("userId"))))
								.defaultIfEmpty(false).map(AuthorizationDecision::new))
						.anyExchange().permitAll())
				.build();
	}
}
