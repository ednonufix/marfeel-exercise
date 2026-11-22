package com.marfeel.exercise.domain.vo;

import java.util.Locale;

import org.jspecify.annotations.NonNull;

public enum UserType {
	U1,
	U2,
	U3;

	public static UserType from(@NonNull String request) {
		return UserType.valueOf(request.toUpperCase(Locale.ROOT));
	}
}
