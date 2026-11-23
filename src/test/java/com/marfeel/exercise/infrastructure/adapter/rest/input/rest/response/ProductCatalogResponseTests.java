package com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response;

import com.marfeel.exercise.domain.vo.UserType;
import com.marfeel.exercise.infrastructure.adapter.rest.input.rest.response.ProductCatalogResponse.ProductDetail;

import java.util.List;

import org.junit.jupiter.api.Test;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogResponseTests {

	@Test
	void writesServiceOnceInEachProductReference() {
		var mapper = JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
		var catalog = new ProductCatalogResponse(UserType.U1, List.of(
			new ProductDetail(new S1Response("S1", "S1-A", "X1"), 12, "ES01"),
			new ProductDetail(new S2Response("S2", "S2-100"), 34, "ES02")));

		var products = mapper.readTree(mapper.writeValueAsString(catalog)).get("products");

		assertThat(products.get(0).get("ref")).isEqualTo(mapper.readTree("""
			{"service":"S1","code":"S1-A","subcode":"X1"}
			"""));
		assertThat(products.get(1).get("ref")).isEqualTo(mapper.readTree("""
			{"service":"S2","itemId":"S2-100"}
			"""));
	}
}
