package com.iemr.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/** Covers the advice that reports bean-validation failures in the AMRIT response envelope. */
class ValidationExceptionHandlerTest {

	private final ValidationExceptionHandler handler = new ValidationExceptionHandler();

	@Test
	@DisplayName("field errors are collected per field under the 5000 status code")
	void fieldErrorsAreCollectedPerField() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getAllErrors()).thenReturn(List.of(new FieldError("form", "phoneNo", "must be 10 digits"),
				new FieldError("form", "name", "must not be blank")));
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("status", "ERROR").containsEntry("statusCode", 5000)
				.containsEntry("errorMessage", "Input validation failed");
		@SuppressWarnings("unchecked")
		Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
		assertThat(errors).containsEntry("phoneNo", "must be 10 digits").containsEntry("name", "must not be blank");
	}

	@Test
	@DisplayName("a request with no errors still reports the failure envelope with an empty error map")
	void noFieldErrorsStillReportsEnvelope() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getAllErrors()).thenReturn(List.of());
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(exception);

		assertThat((Map<?, ?>) response.getBody().get("errors")).isEmpty();
	}

	@Test
	@DisplayName("an illegal argument is reported with its own message")
	void illegalArgumentIsReported() {
		ResponseEntity<Map<String, Object>> response = handler
				.handleIllegalArgument(new IllegalArgumentException("feedbackID is required"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("status", "ERROR").containsEntry("statusCode", 5000)
				.containsEntry("errorMessage", "feedbackID is required");
	}
}
