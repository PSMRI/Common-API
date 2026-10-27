package com.iemr.common.controller.platform_feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.iemr.common.dto.platform_feedback.CategoryResponse;
import com.iemr.common.dto.platform_feedback.FeedbackRequest;
import com.iemr.common.dto.platform_feedback.FeedbackResponse;
import com.iemr.common.service.platform_feedback.PlatformFeedbackService;

/** Covers the public platform-feedback endpoints. */
@ExtendWith(MockitoExtension.class)
class PlatformFeedbackControllerTest {

	@Mock
	private PlatformFeedbackService service;

	@InjectMocks
	private PlatformFeedbackController controller;

	@Test
	@DisplayName("accepted feedback is reported as 201 Created")
	void submittedFeedbackIsCreated() {
		FeedbackRequest request = new FeedbackRequest(4, "app-crash", "The call list will not load", "9999999999", true,
				"1097", 12);
		FeedbackResponse response = new FeedbackResponse("accepted", java.time.LocalDateTime.now());
		when(service.submitFeedback(request)).thenReturn(response);

		ResponseEntity<FeedbackResponse> result = controller.submit(request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isSameAs(response);
	}

	@Test
	@DisplayName("categories are listed for the requested service line")
	void categoriesAreListedForTheRequestedServiceLine() {
		List<CategoryResponse> categories = List.of(new CategoryResponse("app-crash", "App crash", "Crashes", "1097", true));
		when(service.listCategories("1097")).thenReturn(categories);

		ResponseEntity<List<CategoryResponse>> result = controller.list("1097");

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isSameAs(categories);
	}

	@Test
	@DisplayName("omitting the service line falls back to the global category set")
	void missingServiceLineFallsBackToGlobal() {
		when(service.listCategories("GLOBAL")).thenReturn(List.of());

		assertThat(controller.list(null).getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(service).listCategories("GLOBAL");
	}
}
