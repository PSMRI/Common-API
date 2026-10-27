package com.iemr.common.service.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.data.feedback.FeedbackRequest;
import com.iemr.common.data.feedback.FeedbackResponse;
import com.iemr.common.data.feedback.FeedbackSeverity;
import com.iemr.common.data.feedback.FeedbackType;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.repository.feedback.FeedbackRequestRepository;
import com.iemr.common.repository.feedback.FeedbackResponseRepository;
import com.iemr.common.repository.feedback.FeedbackSeverityRepository;
import com.iemr.common.repository.feedback.FeedbackTypeRepository;

/** Covers the small feedback lookup and persistence services. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedbackAncillaryServicesTest {

	@Nested
	@ExtendWith(MockitoExtension.class)
	@MockitoSettings(strictness = Strictness.LENIENT)
	class RequestService {

		@Mock
		private FeedbackRequestRepository feedbackRequestRepository;

		@InjectMocks
		private FeedbackRequestServiceImpl service;

		@Test
		@DisplayName("a request is fetched straight from the repository")
		void getFeedbackRequestDelegates() {
			FeedbackRequest stored = new FeedbackRequest();
			when(feedbackRequestRepository.findByFeedbackRequestID(7L)).thenReturn(stored);

			assertThat(service.getFeedbackReuest(7L)).isSameAs(stored);
		}

		@Test
		@DisplayName("creating a request saves the deserialized payload")
		void createFeedbackRequestSaves() throws Exception {
			FeedbackRequest saved = new FeedbackRequest();
			when(feedbackRequestRepository.save(any(FeedbackRequest.class))).thenReturn(saved);

			String result = service.createFeedbackRequest("{\"feedbackID\":12}");

			assertThat(result).isEqualTo(saved.toString());
			verify(feedbackRequestRepository).save(any(FeedbackRequest.class));
		}

		@Test
		@DisplayName("rows shorter than the expected projection are skipped")
		void getAllFeedbackSkipsShortRows() throws Exception {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[] { 1L, 2L, "subject", 3, "body", 4, null });
			rows.add(null);
			rows.add(new Object[] { 1L, 2L });
			when(feedbackRequestRepository.getAllFeedback(any())).thenReturn(rows);

			String result = service.getAllFeedback("{\"feedbackID\":12}");

			assertThat(result).startsWith("[").endsWith("]").contains("subject");
		}

		@Test
		@DisplayName("an empty projection maps to an empty list")
		void getAllFeedbackWithNoRows() throws Exception {
			when(feedbackRequestRepository.getAllFeedback(any())).thenReturn(new ArrayList<>());

			assertThat(service.getAllFeedback("{\"feedbackID\":12}")).isEqualTo("[]");
		}

		@Test
		@DisplayName("malformed JSON is reported rather than swallowed")
		void createFeedbackRequestRejectsMalformedJson() {
			assertThatThrownBy(() -> service.createFeedbackRequest("not json")).isInstanceOf(Exception.class);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@MockitoSettings(strictness = Strictness.LENIENT)
	class ResponseService {

		@Mock
		private FeedbackResponseRepository feedbackResponseRepository;
		@Mock
		private FeedbackRequestRepository feedbackRequestRepository;
		@Mock
		private FeedbackRepository feedbackRepository;

		private FeedbackResponseServiceImpl service() {
			FeedbackResponseServiceImpl service = new FeedbackResponseServiceImpl();
			service.setFeedbackResponseRepository(feedbackResponseRepository);
			service.setFeedbackRequestRepository(feedbackRequestRepository);
			service.setFeedbackRepository(feedbackRepository);
			return service;
		}

		@Test
		@DisplayName("a response is fetched by its id")
		void getFeedbackResponseDelegates() {
			FeedbackResponse stored = new FeedbackResponse();
			when(feedbackResponseRepository.findByFeedbackResponseID(3L)).thenReturn(stored);

			assertThat(service().getFeedbackResponse(3L)).isSameAs(stored);
		}

		@Test
		@DisplayName("creating a response and a request both persist through their repositories")
		void createDelegatesToRepositories() {
			FeedbackResponse response = new FeedbackResponse();
			FeedbackRequest request = new FeedbackRequest();
			when(feedbackResponseRepository.save(response)).thenReturn(response);
			when(feedbackRequestRepository.save(request)).thenReturn(request);

			FeedbackResponseServiceImpl service = service();
			assertThat(service.createFeedbackResponse(response)).isSameAs(response);
			assertThat(service.createFeedbackRequest(request)).isSameAs(request);
		}

		@Test
		@DisplayName("updating a response saves the deserialized payload")
		void updateResponseSaves() throws Exception {
			FeedbackResponse saved = new FeedbackResponse();
			when(feedbackResponseRepository.save(any(FeedbackResponse.class))).thenReturn(saved);

			assertThat(service().updateResponce("{\"feedbackResponseID\":5}")).isEqualTo(saved.toString());
		}

		@Test
		@DisplayName("the projection for a feedback id is passed through untouched")
		void getDataByIdDelegates() {
			ArrayList<Object[]> rows = new ArrayList<>();
			rows.add(new Object[] { 1 });
			when(feedbackResponseRepository.getdatabyId(9L)).thenReturn(rows);

			assertThat(service().getdataById(9L)).isSameAs(rows);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@MockitoSettings(strictness = Strictness.LENIENT)
	class TypeService {

		@Mock
		private FeedbackTypeRepository feedbackTypeRepository;

		private FeedbackTypeServiceImpl service() {
			FeedbackTypeServiceImpl service = new FeedbackTypeServiceImpl();
			service.setFeedbackTypeRepository(feedbackTypeRepository);
			return service;
		}

		@Test
		@DisplayName("active types are built from the id/name projection, short rows skipped")
		void activeTypesAreMapped() {
			Set<Object[]> rows = new LinkedHashSet<>();
			rows.add(new Object[] { 1, "Complaint" });
			rows.add(null);
			rows.add(new Object[] { 2 });
			when(feedbackTypeRepository.findActiveFeedbackTypes()).thenReturn(rows);

			List<FeedbackType> types = service().getActiveFeedbackTypes();

			assertThat(types).hasSize(1);
			assertThat(types.get(0).getFeedbackTypeName()).isEqualTo("Complaint");
		}

		@Test
		@DisplayName("the provider-scoped lookup is returned as the repository gave it")
		void activeTypesForProviderDelegate() {
			List<FeedbackType> stored = List.of(new FeedbackType(4, "Query"));
			when(feedbackTypeRepository.findActiveFeedbackTypes(anyInt())).thenReturn(stored);

			assertThat(service().getActiveFeedbackTypes(11)).isEqualTo(stored);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@MockitoSettings(strictness = Strictness.LENIENT)
	class SeverityService {

		@Mock
		private FeedbackSeverityRepository feedbackSeverityRepository;

		private FeedbackSeverityServiceImpl service() {
			FeedbackSeverityServiceImpl service = new FeedbackSeverityServiceImpl();
			service.setFeedbackTypeRepository(feedbackSeverityRepository);
			return service;
		}

		@Test
		@DisplayName("active severities are built from the id/name projection, short rows skipped")
		void activeSeveritiesAreMapped() {
			Set<Object[]> rows = new LinkedHashSet<>();
			rows.add(new Object[] { 1, "High" });
			rows.add(null);
			rows.add(new Object[] { 2 });
			when(feedbackSeverityRepository.getActiveFeedbackSeverity()).thenReturn(rows);

			List<FeedbackSeverity> severities = service().getActiveFeedbackSeverity();

			assertThat(severities).hasSize(1);
			assertThat(severities.get(0).getSeverityTypeName()).isEqualTo("High");
		}

		@Test
		@DisplayName("the provider-scoped lookup is returned as the repository gave it")
		void activeSeveritiesForProviderDelegate() {
			List<FeedbackSeverity> stored = List.of(new FeedbackSeverity(2, "Low"));
			when(feedbackSeverityRepository.getActiveFeedbackSeverity(anyInt())).thenReturn(stored);

			assertThat(service().getActiveFeedbackSeverity(11)).isEqualTo(stored);
		}
	}
}
