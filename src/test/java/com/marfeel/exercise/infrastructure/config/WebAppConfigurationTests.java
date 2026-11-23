package com.marfeel.exercise.infrastructure.config;

import com.marfeel.exercise.domain.vo.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.format.support.DefaultFormattingConversionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebAppConfigurationTests {

	@Test
	void registersCaseInsensitiveUserTypeConversionAndRejectsUnknownUsers() {
		var formatters = new DefaultFormattingConversionService();
		new WebAppConfiguration().addFormatters(formatters);

		assertThat(formatters.convert("u2", UserType.class)).isEqualTo(UserType.U2);
		assertThatThrownBy(() -> formatters.convert("U9", UserType.class)).isInstanceOf(ConversionFailedException.class);
	}
}
