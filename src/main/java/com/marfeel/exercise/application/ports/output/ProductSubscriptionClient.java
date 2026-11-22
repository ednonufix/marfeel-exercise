package com.marfeel.exercise.application.ports.output;

import com.marfeel.exercise.application.dto.ProviderSubscriptionResult;
import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;
import reactor.core.publisher.Mono;

public interface ProductSubscriptionClient {

	Mono<ProviderSubscriptionResult> subscribe(UserType userType, ProductRef product);
}
