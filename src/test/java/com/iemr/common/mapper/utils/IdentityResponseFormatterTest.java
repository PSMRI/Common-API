package com.iemr.common.mapper.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Covers the unwrapping of the identity service's nested response envelope. */
class IdentityResponseFormatterTest {

	@Test
	@DisplayName("the data payload is lifted out of the response envelope")
	void dataIsLiftedOutOfTheEnvelope() {
		String formatted = IdentityResponseFormatter
				.getFormattedString("{\"response\":{\"data\":\"[{\\\"benRegId\\\":1}]\"}}");

		assertThat(formatted).isEqualTo("[{\"benRegId\":1}]");
	}

	@Test
	@DisplayName("an envelope without a response object is rejected")
	void missingResponseObjectIsRejected() {
		assertThatThrownBy(() -> IdentityResponseFormatter.getFormattedString("{\"status\":\"ok\"}"))
				.isInstanceOf(JSONException.class);
	}

	@Test
	@DisplayName("a response without a data field is rejected")
	void missingDataFieldIsRejected() {
		assertThatThrownBy(() -> IdentityResponseFormatter.getFormattedString("{\"response\":{}}"))
				.isInstanceOf(JSONException.class);
	}

	@Test
	@DisplayName("a formatter instance is constructible for the container's benefit")
	void formatterIsConstructible() {
		assertThat(new IdentityResponseFormatter()).isNotNull();
	}
}
