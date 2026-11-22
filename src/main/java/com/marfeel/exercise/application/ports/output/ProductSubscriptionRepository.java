package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.application.dto.SaveSubscriptionCommand;
import reactor.core.publisher.Mono;

public interface ProductSubscriptionRepository {

	Mono<Void> save(SaveSubscriptionCommand command);

}
