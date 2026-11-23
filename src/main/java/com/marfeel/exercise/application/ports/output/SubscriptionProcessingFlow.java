package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

import java.util.Optional;

public interface SubscriptionProcessingFlow {

	Optional<Reservation> tryAcquire(UserType user, ProductRef product);

	interface Reservation {
		void release();
	}
}
