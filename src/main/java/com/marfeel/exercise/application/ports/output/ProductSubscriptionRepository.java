package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;
import reactor.core.publisher.Mono;

public interface ProductSubscriptionRepository {

	Mono<Boolean> isActive(UserType user, ProductRef product);

	Mono<Void> save(SaveSubscriptionCommand command);

}
