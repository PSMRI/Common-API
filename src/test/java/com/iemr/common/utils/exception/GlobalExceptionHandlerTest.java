package com.iemr.common.utils.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import com.iemr.common.utils.response.ApiResponse;

/** Covers the advice that turns unhandled exceptions into an {@link ApiResponse}. */
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	@DisplayName("an unexpected exception becomes a generic 500 that leaks no internals")
	void unexpectedExceptionBecomesGenericServerError() {
		ResponseEntity<ApiResponse<Object>> response = handler
				.handleException(new IllegalStateException("connection string root:secret@db"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong");
		assertThat(response.getBody().getStatusCode()).isEqualTo(500);
		assertThat(response.getBody().getData()).isNull();
	}

	@Test
	@DisplayName("every field error is listed in the validation message")
	void validationErrorsAreJoined() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("form", "name", "must not be blank"),
				new FieldError("form", "age", "must be positive")));
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<ApiResponse<Object>> response = handler.handleValidationException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getMessage())
				.isEqualTo("Validation Error: name: must not be blank, age: must be positive");
		assertThat(response.getBody().getStatusCode()).isEqualTo(400);
	}

	@Test
	@DisplayName("a request with no field errors still reports a 400")
	void validationWithoutFieldErrors() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(List.of());
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<ApiResponse<Object>> response = handler.handleValidationException(exception);

		assertThat(response.getBody().getMessage()).isEqualTo("Validation Error: ");
	}

	@Test
	@DisplayName("an illegal argument keeps its own message")
	void illegalArgumentKeepsItsMessage() {
		ResponseEntity<ApiResponse<Object>> response = handler
				.handleIllegalArgException(new IllegalArgumentException("providerServiceMapID is required"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getMessage()).isEqualTo("providerServiceMapID is required");
	}

	@Test
	@DisplayName("a ResponseStatusException keeps its status and reason")
	void responseStatusExceptionKeepsStatusAndReason() {
		ResponseEntity<ApiResponse<Object>> response = handler
				.handleResponseStatusException(new ResponseStatusException(HttpStatus.NOT_FOUND, "no such beneficiary"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().getMessage()).isEqualTo("no such beneficiary");
		assertThat(response.getBody().getStatusCode()).isEqualTo(404);
	}

	@Test
	@DisplayName("a ResponseStatusException without a reason falls back to its message")
	void responseStatusExceptionWithoutReason() {
		ResponseEntity<ApiResponse<Object>> response = handler
				.handleResponseStatusException(new ResponseStatusException(HttpStatus.CONFLICT));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().getMessage()).contains("409");
	}
}
