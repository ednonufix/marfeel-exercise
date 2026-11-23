package com.marfeel.exercise.application.ports.output;

import java.util.Optional;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

public interface SubscriptionProcessingFlow {

	Optional<Reservation> tryAcquire(UserType user, ProductRef product);

	interface Reservation {
		void release();
	}
}
