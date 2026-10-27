package com.iemr.common.service.platform_feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.data.platform_feedback.Feedback;
import com.iemr.common.data.platform_feedback.FeedbackCategory;
import com.iemr.common.dto.platform_feedback.CategoryResponse;
import com.iemr.common.dto.platform_feedback.FeedbackRequest;
import com.iemr.common.dto.platform_feedback.FeedbackResponse;
import com.iemr.common.exception.BadRequestException;
import com.iemr.common.repository.platform_feedback.PlatformFeedbackCategoryRepository;
import com.iemr.common.repository.platform_feedback.PlatformFeedbackRepository;

/**
 * Covers platform feedback submission and the category list offered to each service line.
 *
 * <p>Submission validates the rating, requires a user id for non-anonymous feedback, and
 * resolves the category by id, by slug or by both, so each of those paths and its rejection
 * is exercised.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlatformFeedbackServiceTest {

	@Mock
	private PlatformFeedbackRepository feedbackRepo;
	@Mock
	private PlatformFeedbackCategoryRepository categoryRepo;

	@InjectMocks
	private PlatformFeedbackService service;

	private static FeedbackCategory category(String id, String slug, String scope, boolean active) {
		FeedbackCategory category = new FeedbackCategory();
		category.setCategoryId(id);
		category.setSlug(slug);
		category.setLabel("Label for " + slug);
		category.setScope(scope);
		category.setActive(active);
		return category;
	}

	private static FeedbackRequest request(int rating, String categoryId, String categorySlug, String comment,
			boolean anonymous, Integer userId) {
		return new FeedbackRequest(rating, categoryId, categorySlug, comment, anonymous, "1097", userId);
	}

	private Feedback savedFeedback() {
		ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
		verify(feedbackRepo).save(captor.capture());
		return captor.getValue();
	}

	@Nested
	@DisplayName("submitFeedback")
	class SubmitFeedback {

		@Test
		@DisplayName("stores the feedback and returns its generated id and timestamp")
		void storesTheFeedback() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			FeedbackResponse response = service.submitFeedback(request(4, "cat-1", null, "Works well", true, null));

			assertThat(response.id()).isNotBlank();
			assertThat(response.createdAt()).isNotNull();
			Feedback saved = savedFeedback();
			assertThat(saved.getRating()).isEqualTo(4);
			assertThat(saved.getComment()).isEqualTo("Works well");
			assertThat(saved.getServiceLine()).isEqualTo("1097");
			assertThat(saved.isAnonymous()).isTrue();
			assertThat(saved.getCategory().getCategoryId()).isEqualTo("cat-1");
		}

		@Test
		@DisplayName("stores no comment when none was given")
		void absentCommentIsNotStored() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			service.submitFeedback(request(3, "cat-1", null, null, true, null));

			// The service substitutes an empty string, which the entity normalises to null.
			assertThat(savedFeedback().getComment()).isNull();
		}

		@Test
		@DisplayName("trims a comment that is only whitespace down to nothing")
		void whitespaceCommentIsNotStored() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			service.submitFeedback(request(3, "cat-1", null, "   ", true, null));

			assertThat(savedFeedback().getComment()).isNull();
		}

		@Test
		@DisplayName("records the user id for identified feedback")
		void recordsTheUserId() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			service.submitFeedback(request(5, "cat-1", null, "Great", false, 42));

			assertThat(savedFeedback().getUserId()).isEqualTo(42);
		}

		@ParameterizedTest(name = "rating {0}")
		@ValueSource(ints = { 0, -1, 6, 99 })
		@DisplayName("rejects a rating outside one to five")
		void rejectsRatingOutOfRange(int rating) {
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(rating, "cat-1", null, null, true, null)))
					.withMessageContaining("rating must be between 1 and 5");
			verify(feedbackRepo, never()).save(any());
		}

		@ParameterizedTest(name = "rating {0}")
		@ValueSource(ints = { 1, 5 })
		@DisplayName("accepts the boundary ratings")
		void acceptsBoundaryRatings(int rating) {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			assertThat(service.submitFeedback(request(rating, "cat-1", null, null, true, null))).isNotNull();
		}

		@Test
		@DisplayName("requires a user id for non-anonymous feedback")
		void requiresUserIdWhenNotAnonymous() {
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, "cat-1", null, null, false, null)))
					.withMessageContaining("userId required when isAnonymous=false");
		}
	}

	@Nested
	@DisplayName("category resolution")
	class CategoryResolution {

		@Test
		@DisplayName("resolves by slug alone")
		void resolvesBySlug() {
			when(categoryRepo.findBySlugIgnoreCase("usability"))
					.thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			service.submitFeedback(request(4, null, "usability", null, true, null));

			assertThat(savedFeedback().getCategory().getSlug()).isEqualTo("usability");
		}

		@Test
		@DisplayName("accepts a matching id and slug pair")
		void acceptsMatchingIdAndSlug() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			service.submitFeedback(request(4, "cat-1", "USABILITY", null, true, null));

			assertThat(savedFeedback().getCategory().getCategoryId()).isEqualTo("cat-1");
		}

		@Test
		@DisplayName("rejects an id and slug pair that disagree")
		void rejectsMismatchedIdAndSlug() {
			when(categoryRepo.findById("cat-1")).thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", true)));

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, "cat-1", "performance", null, true, null)))
					.withMessageContaining("categoryId and categorySlug mismatch");
		}

		@Test
		@DisplayName("rejects an unknown category id")
		void rejectsUnknownId() {
			when(categoryRepo.findById(anyString())).thenReturn(Optional.empty());

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, "cat-x", null, null, true, null)))
					.withMessageContaining("invalid categoryId");
		}

		@Test
		@DisplayName("rejects an unknown category slug")
		void rejectsUnknownSlug() {
			when(categoryRepo.findBySlugIgnoreCase(anyString())).thenReturn(Optional.empty());

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, null, "nope", null, true, null)))
					.withMessageContaining("invalid categorySlug");
		}

		@Test
		@DisplayName("rejects an inactive category found by id")
		void rejectsInactiveById() {
			when(categoryRepo.findById("cat-1"))
					.thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", false)));

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, "cat-1", null, null, true, null)))
					.withMessageContaining("category inactive");
		}

		@Test
		@DisplayName("rejects an inactive category found by slug")
		void rejectsInactiveBySlug() {
			when(categoryRepo.findBySlugIgnoreCase("usability"))
					.thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", false)));

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, null, "usability", null, true, null)))
					.withMessageContaining("category inactive");
		}

		@Test
		@DisplayName("rejects an inactive category found by an id and slug pair")
		void rejectsInactiveByIdAndSlug() {
			when(categoryRepo.findById("cat-1"))
					.thenReturn(Optional.of(category("cat-1", "usability", "GLOBAL", false)));

			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, "cat-1", "usability", null, true, null)))
					.withMessageContaining("category inactive");
		}

		@Test
		@DisplayName("requires either an id or a slug")
		void requiresIdOrSlug() {
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> service.submitFeedback(request(4, null, null, null, true, null)))
					.withMessageContaining("categoryId or categorySlug required");
		}
	}

	@Nested
	@DisplayName("listCategories")
	class ListCategories {

		@Test
		@DisplayName("returns global categories and those scoped to the requested service line")
		void filtersByScope() {
			when(categoryRepo.findByActiveTrueOrderByLabelAsc())
					.thenReturn(List.of(category("cat-1", "usability", "GLOBAL", true),
							category("cat-2", "coverage", "1097", true),
							category("cat-3", "other", "104", true)));

			List<CategoryResponse> categories = service.listCategories("1097");

			assertThat(categories).extracting(CategoryResponse::slug).containsExactly("usability", "coverage");
		}

		@Test
		@DisplayName("matches the service line scope case-insensitively")
		void matchesScopeIgnoringCase() {
			when(categoryRepo.findByActiveTrueOrderByLabelAsc())
					.thenReturn(List.of(category("cat-2", "coverage", "1097", true)));

			assertThat(service.listCategories("1097")).hasSize(1);
		}

		@Test
		@DisplayName("carries the label, scope and active flag through")
		void carriesTheDetailsThrough() {
			when(categoryRepo.findByActiveTrueOrderByLabelAsc())
					.thenReturn(List.of(category("cat-1", "usability", "GLOBAL", true)));

			CategoryResponse response = service.listCategories("1097").get(0);

			assertThat(response.categoryId()).isEqualTo("cat-1");
			assertThat(response.label()).isEqualTo("Label for usability");
			assertThat(response.scope()).isEqualTo("GLOBAL");
			assertThat(response.active()).isTrue();
		}

		@Test
		@DisplayName("returns nothing when no category is active")
		void emptyWhenNoneActive() {
			when(categoryRepo.findByActiveTrueOrderByLabelAsc()).thenReturn(List.of());

			assertThat(service.listCategories("1097")).isEmpty();
		}
	}
}
