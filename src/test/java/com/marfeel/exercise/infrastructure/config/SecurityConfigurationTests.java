package com.marfeel.exercise.infrastructure.config;

import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTests {

	@Test
	void registersEveryRegistryUserWithAnEncodedPasswordAndUserRole() {
		var users = new SecurityConfiguration().registryUsers();
		var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		for (UserType user : UserType.values()) {
			StepVerifier.create(users.findByUsername(user.name()))
				.assertNext(details -> {
					assertThat(details.getUsername()).isEqualTo(user.name());
					assertThat(details.getPassword()).isNotEqualTo(user.name());
					assertThat(encoder.matches(user.name(), details.getPassword())).isTrue();
					assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly(
						"ROLE_USER");
				}).expectComplete().verify(Duration.ofSeconds(5));
		}
		StepVerifier.create(users.findByUsername("unknown")).expectComplete().verify(Duration.ofSeconds(5));
	}
}
