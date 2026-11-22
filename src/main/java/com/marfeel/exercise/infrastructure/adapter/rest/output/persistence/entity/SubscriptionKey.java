package com.marfeel.exercise.infrastructure.adapter.rest.output.persistence.entity;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.model.S1Product;
import com.marfeel.exercise.domain.model.S2Product;
import com.marfeel.exercise.domain.vo.UserType;
import org.springframework.data.relational.core.query.Criteria;

public record SubscriptionKey(String userId, String service, String productCode, String productSubcode) {

	public static SubscriptionKey of(UserType user, ProductRef product) {
		return switch (product) {
			case S1Product s1 -> new SubscriptionKey(user.name(), "S1", s1.code(), s1.subcode());
			case S2Product s2 -> new SubscriptionKey(user.name(), "S2", s2.itemId(), "");
		};
	}

	public Criteria criteria() {
		return Criteria.where("userId").is(userId).and("service").is(service)
				.and("productCode").is(productCode).and("productSubcode").is(productSubcode);
	}
}
