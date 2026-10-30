package com.iemr.common.notification.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ConnectException;
import java.rmi.ConnectIOException;
import java.sql.SQLException;
import java.text.ParseException;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import com.google.gson.JsonObject;
import com.iemr.common.notification.exception.IEMRException;

import java.util.stream.Stream;

/**
 * Covers the notification module's response envelope and its error classification.
 *
 * <p>The builder maps an exception type onto a status code and a caller-facing message, so
 * each mapped family is asserted alongside the default fallback.
 */
class OutputResponseTest {

	private static JsonObject responseOf(OutputResponse response) {
		return response.getResponse().getAsJsonObject();
	}

	@Nested
	@DisplayName("build")
	class Build {

		@Test
		@DisplayName("a fresh builder produces an empty envelope with a zero status")
		void emptyEnvelope() {
			JsonObject payload = responseOf(new OutputResponse.Builder().build());

			assertThat(payload.get("methodName").getAsString()).isEmpty();
			assertThat(payload.get("dataObjectType").getAsString()).isEmpty();
			assertThat(payload.get("dataJsonType").getAsString()).isEmpty();
			assertThat(payload.get("data").getAsString()).isEmpty();
			assertThat(payload.get("statusCode").getAsInt()).isZero();
			assertThat(payload.get("statusMessage").getAsString()).isEmpty();
			assertThat(payload.get("statusMessageLong").getAsString()).isEmpty();
		}

		@Test
		@DisplayName("every field set on the builder appears in the envelope")
		void carriesEveryField() {
			OutputResponse response = new OutputResponse.Builder().setMethodName("getUser")
					.setDataObjectType("User").setDataJsonType("object").setData("{\"a\":1}")
					.setStatusCode(OutputResponse.Builder.SUCCESS).setStatusMessage("Success")
					.setStatusMessageLong("Everything worked").build();

			JsonObject payload = responseOf(response);
			assertThat(payload.get("methodName").getAsString()).isEqualTo("getUser");
			assertThat(payload.get("dataObjectType").getAsString()).isEqualTo("User");
			assertThat(payload.get("dataJsonType").getAsString()).isEqualTo("object");
			assertThat(payload.get("data").getAsString()).isEqualTo("{\"a\":1}");
			assertThat(payload.get("statusCode").getAsInt()).isEqualTo(200);
			assertThat(payload.get("statusMessage").getAsString()).isEqualTo("Success");
			assertThat(payload.get("statusMessageLong").getAsString()).isEqualTo("Everything worked");
		}

		@Test
		@DisplayName("the builder returns itself so calls can be chained")
		void isChainable() {
			OutputResponse.Builder builder = new OutputResponse.Builder();

			assertThat(builder.setMethodName("m")).isSameAs(builder);
			assertThat(builder.setDataObjectType("t")).isSameAs(builder);
			assertThat(builder.setDataJsonType("j")).isSameAs(builder);
			assertThat(builder.setData("d")).isSameAs(builder);
			assertThat(builder.setStatusCode(1)).isSameAs(builder);
			assertThat(builder.setStatusMessage("s")).isSameAs(builder);
			assertThat(builder.setStatusMessageLong("l")).isSameAs(builder);
			assertThat(builder.setErrorMessage(new RuntimeException("boom"))).isSameAs(builder);
		}
	}

	@Nested
	@DisplayName("setErrorMessage")
	class ErrorClassification {

		static Stream<Arguments> mappedExceptions() {
			return Stream.of(
					Arguments.of(new IEMRException("login failed"), OutputResponse.Builder.USERID_FAILURE,
							"User login failed"),
					Arguments.of(new JSONException("bad json"), OutputResponse.Builder.OBJECT_FAILURE,
							"Invalid object conversion"),
					Arguments.of(new SQLException("db error"), OutputResponse.Builder.CODE_EXCEPTION,
							"Failed with critical errors"),
					Arguments.of(new ParseException("bad date", 0), OutputResponse.Builder.CODE_EXCEPTION,
							"Failed with critical errors"),
					Arguments.of(new NullPointerException("npe"), OutputResponse.Builder.CODE_EXCEPTION,
							"Failed with critical errors"),
					Arguments.of(new ArrayIndexOutOfBoundsException("oob"),
							OutputResponse.Builder.CODE_EXCEPTION, "Failed with critical errors"),
					Arguments.of(new IOException("io"), OutputResponse.Builder.ENVIRONMENT_EXCEPTION,
							"Failed with connection issues"),
					Arguments.of(new ConnectException("refused"), OutputResponse.Builder.ENVIRONMENT_EXCEPTION,
							"Failed with connection issues"),
					Arguments.of(new IllegalStateException("something else"),
							OutputResponse.Builder.GENERIC_FAILURE, "Failed with generic exception"));
		}

		@ParameterizedTest(name = "{0} maps to {1}")
		@MethodSource("mappedExceptions")
		@DisplayName("classifies a failure by its exception type")
		void classifiesFailures(Throwable thrown, int expectedStatusCode, String expectedMessageFragment) {
			JsonObject payload = responseOf(new OutputResponse.Builder().setErrorMessage(thrown).build());

			assertThat(payload.get("statusCode").getAsInt()).isEqualTo(expectedStatusCode);
			assertThat(payload.get("statusMessage").getAsString()).contains(expectedMessageFragment);
		}

		@Test
		@DisplayName("carries the original message through as the long status message")
		void carriesTheOriginalMessage() {
			JsonObject payload = responseOf(
					new OutputResponse.Builder().setErrorMessage(new IEMRException("login failed")).build());

			assertThat(payload.get("statusMessageLong").getAsString()).isEqualTo("login failed");
		}

		@Test
		@DisplayName("classifies a remote connection failure as an environment problem")
		void classifiesRemoteConnectionFailure() {
			JsonObject payload = responseOf(new OutputResponse.Builder()
					.setErrorMessage(new ConnectIOException("remote down")).build());

			assertThat(payload.get("statusCode").getAsInt())
					.isEqualTo(OutputResponse.Builder.ENVIRONMENT_EXCEPTION);
		}
	}

	@Test
	@DisplayName("the envelope serialises to JSON carrying its response element")
	void serialisesToJson() {
		String json = new OutputResponse.Builder().setMethodName("getUser").setStatusCode(200).build().toString();

		assertThat(json).contains("\"response\"").contains("\"methodName\":\"getUser\"")
				.contains("\"statusCode\":200");
	}

	@Test
	@DisplayName("the status code constants are the documented values")
	void statusCodeConstants() {
		assertThat(OutputResponse.Builder.SUCCESS).isEqualTo(200);
		assertThat(OutputResponse.Builder.GENERIC_FAILURE).isEqualTo(5000);
		assertThat(OutputResponse.Builder.OBJECT_FAILURE).isEqualTo(5001);
		assertThat(OutputResponse.Builder.USERID_FAILURE).isEqualTo(5002);
		assertThat(OutputResponse.Builder.PASSWORD_FAILURE).isEqualTo(5003);
		assertThat(OutputResponse.Builder.PRIVILEGE_FAILURE).isEqualTo(5004);
		assertThat(OutputResponse.Builder.CODE_EXCEPTION).isEqualTo(5005);
		assertThat(OutputResponse.Builder.ENVIRONMENT_EXCEPTION).isEqualTo(5006);
		assertThat(OutputResponse.Builder.PARSE_EXCEPTION).isEqualTo(5007);
		assertThat(OutputResponse.Builder.MANDATORY_PARAMS_MISSING).isEqualTo(5008);
		assertThat(OutputResponse.Builder.ILLEGAL_ACTION).isEqualTo(5009);
	}
}
