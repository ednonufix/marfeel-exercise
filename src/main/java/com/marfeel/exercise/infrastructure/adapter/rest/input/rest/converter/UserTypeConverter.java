package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.converter;

import com.marfeel.exercise.domain.vo.UserType;

import org.springframework.core.convert.converter.Converter;

import org.jspecify.annotations.NonNull;

public class UserTypeConverter implements Converter<String, UserType> {

	@Override
	public UserType convert(@NonNull String value) {
		return UserType.from(value);
	}
}
