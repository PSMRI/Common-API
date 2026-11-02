package com.iemr.common.service.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.report.CallDetailsReport;
import com.iemr.common.data.report.CallReport;
import com.iemr.common.data.report.DimUserReport;
import com.iemr.common.data.report.MedHistory;
import com.iemr.common.data.report.ReportType;
import com.iemr.common.data.report.UnBlockedPhoneReport;
import com.iemr.common.mapper.Report1097Mapper;
import com.iemr.common.model.reports.UnBlockedUserReport;
import com.iemr.common.repository.report.CRMCallReportRepo;
import com.iemr.common.repository.report.CallReportRepo;
import com.iemr.common.utils.exception.IEMRException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CallReportsServiceImplTest {

	private static final String CTI_SERVER_IP = "10.20.30.40";
	private static final String CALL_INFO_URL = "http://CTI_SERVER/info?agent=AGENT_ID&session=SESSION_ID&phone=PHONE_NO";

	@Mock
	private CRMCallReportRepo crmCallReportRepo;

	@Mock
	private CallReportRepo callReportRepo;

	@Mock
	private Report1097Mapper report1097Mapper;

	private EntityManager entityManager;

	private CallReportsServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CallReportsServiceImpl();
		service.setCrmCallReportRepository(crmCallReportRepo);
		entityManager = mock(EntityManager.class, RETURNS_DEEP_STUBS);
		ReflectionTestUtils.setField(service, "callReportRepo", callReportRepo);
		ReflectionTestUtils.setField(service, "mapper", report1097Mapper);
		ReflectionTestUtils.setField(service, "entityManager", entityManager);
		ReflectionTestUtils.setField(service, "ctiServerIP", CTI_SERVER_IP);
		ReflectionTestUtils.setField(service, "callinfoapiURL", CALL_INFO_URL);
	}

	/** Points the criteria query built by the service at a canned result list. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void criteriaQueryReturns(List<?> results) {
		TypedQuery query = mock(TypedQuery.class);
		when(query.getResultList()).thenReturn(new ArrayList(results));
		doReturn(query).when(entityManager).createQuery(any(CriteriaQuery.class));
	}

	private static CallDetailsReport callDetailsRequest() {
		CallDetailsReport request = new CallDetailsReport();
		request.getBenReport().setGender("Female");
		request.getBenReport().setState("Karnataka");
		request.getBenReport().setDistrict("Bengaluru");
		request.getBenReport().setPreferredLanguage("Kannada");
		request.getBenReport().setSexualOrientation("Heterosexual");
		request.setBeneficiaryCallType("Inbound");
		request.setBeneficiaryCallSubType("Medical Advice");
		request.setStartTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));
		request.setEndTimestamp(Timestamp.valueOf("2024-01-31 23:59:59"));
		request.setProviderServiceMapID(12);
		request.setMinAge(10);
		request.setMaxAge(20);
		return request;
	}

	@Nested
	@DisplayName("getAllReportsByDate")
	class GetAllReportsByDate {

		@Test
		@DisplayName("returns the rows the criteria query produces")
		void returnsQueryResults() throws Exception {
			CallDetailsReport row = new CallDetailsReport();
			criteriaQueryReturns(List.of(row));

			List<CallDetailsReport> result = service.getAllReportsByDate(callDetailsRequest());

			assertThat(result).containsExactly(row);
		}

		@Test
		@DisplayName("falls back to a default seven day window when no dates are supplied")
		void defaultsTheDateWindow() throws Exception {
			CallDetailsReport request = new CallDetailsReport();
			request.setProviderServiceMapID(7);
			criteriaQueryReturns(List.of());

			assertThat(service.getAllReportsByDate(request)).isEmpty();
		}
	}

	@Nested
	@DisplayName("getReportTypes")
	class GetReportTypes {

		@Test
		@DisplayName("wraps the report type master in a qaReportTypes envelope")
		void returnsReportTypes() throws Exception {
			when(crmCallReportRepo.getReportType()).thenReturn(List.of(new ReportType()));

			assertThat(service.getReportTypes(1)).contains("qaReportTypes");
		}

		@Test
		@DisplayName("fails when the report type master cannot be read")
		void failsWhenMasterMissing() {
			when(crmCallReportRepo.getReportType()).thenReturn(null);

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.getReportTypes(1))
					.withMessageContaining("Error in fetching report type master");
		}
	}

	@Nested
	@DisplayName("demographic breakdown reports")
	class DemographicReports {

		@Test
		@DisplayName("age group report totals the per-group counts and adds an 'all' row")
		void ageGroupReport() throws Exception {
			criteriaQueryReturns(List.of(new CallDetailsReport(), new CallDetailsReport()));

			String response = service.getAllByAgeGroup(List.of(callDetailsRequest()));

			assertThat(response).contains("10 - 20").contains("\"groupName\":\"all\"")
					.contains("serviceProvidedRatio");
		}

		@Test
		@DisplayName("age group report reports a zero ratio when nothing matched")
		void ageGroupReportWithNoCalls() throws Exception {
			criteriaQueryReturns(List.of());

			assertThat(service.getAllByAgeGroup(List.of(callDetailsRequest()))).contains("serviceProvidedRatio");
		}

		@Test
		@DisplayName("gender report groups by the requested genders")
		void genderReport() throws Exception {
			criteriaQueryReturns(List.of(new CallDetailsReport()));

			assertThat(service.getAllByGender(List.of(callDetailsRequest()))).contains("\"gender\":\"Female\"")
					.contains("\"gender\":\"all\"");
		}

		@Test
		@DisplayName("sexual orientation report groups by the requested orientations")
		void sexualOrientationReport() throws Exception {
			criteriaQueryReturns(List.of(new CallDetailsReport()));

			assertThat(service.getAllBySexualOrientation(List.of(callDetailsRequest())))
					.contains("\"sexualOrientation\":\"Heterosexual\"").contains("\"sexualOrientation\":\"all\"");
		}

		@Test
		@DisplayName("preferred language report groups by the requested languages")
		void preferredLanguageReport() throws Exception {
			criteriaQueryReturns(List.of(new CallDetailsReport()));

			assertThat(service.getCountsByPreferredLanguage(List.of(callDetailsRequest())))
					.contains("\"preferredLanguage\":\"Kannada\"").contains("\"preferredLanguage\":\"all\"");
		}
	}

	@Nested
	@DisplayName("getComplaintDetailReport")
	class ComplaintDetailReport {

		@Test
		@DisplayName("counts by feedback nature when a nature id is given")
		void countsByFeedbackNature() throws Exception {
			when(crmCallReportRepo.getFeedbackByFeedbackNatureID(any(), any(), anyInt(), anyInt())).thenReturn(4L);

			String request = "[{\"feedbackTypeName\":\"Grievance\",\"feedbackNatureID\":3,"
					+ "\"feedbackTypeID\":1,\"providerServiceMapID\":12}]";

			assertThat(service.getComplaintDetailReport(request)).contains("4");
			Mockito.verify(crmCallReportRepo).getFeedbackByFeedbackNatureID(any(), any(), anyInt(), anyInt());
		}

		@Test
		@DisplayName("counts by feedback type when no nature id is given")
		void countsByFeedbackType() throws Exception {
			when(crmCallReportRepo.getFeedbackByFeedbackTypeID(any(), any(), anyInt(), anyInt())).thenReturn(9L);

			String request = "[{\"feedbackTypeName\":\"Feedback\",\"feedbackTypeID\":1,\"providerServiceMapID\":12}]";

			assertThat(service.getComplaintDetailReport(request)).contains("9");
			Mockito.verify(crmCallReportRepo).getFeedbackByFeedbackTypeID(any(), any(), anyInt(), anyInt());
		}
	}

	@Nested
	@DisplayName("getUnblockedUserReport")
	class UnblockedUserReport {

		@Test
		@DisplayName("numbers the mapped unblocked users from one upwards")
		void numbersTheRows() throws Exception {
			when(crmCallReportRepo.getUnBlockedUser(any(), any(), anyInt()))
					.thenReturn(new ArrayList<>(List.of(new UnBlockedPhoneReport(), new UnBlockedPhoneReport())));
			when(report1097Mapper.mapUnblockedUserReport(any(UnBlockedPhoneReport.class)))
					.thenAnswer(invocation -> new UnBlockedUserReport());

			String request = "{\"providerServiceMapID\":12}";

			assertThat(service.getUnblockedUserReport(request)).isNotNull();
			Mockito.verify(report1097Mapper, Mockito.times(2)).mapUnblockedUserReport(any());
		}
	}

	@Nested
	@DisplayName("getCallQualityReport")
	class CallQualityReport {

		private String request(String searchCriteria, String extraJson) {
			return "{\"searchCriteria\":\"" + searchCriteria + "\",\"providerServiceMapID\":12" + extraJson + "}";
		}

		@Test
		@DisplayName("call type wise report filters by call type when one is given")
		void callTypeWiseFilteredByCallType() throws Exception {
			when(crmCallReportRepo.getCallTypeWiseReportByCallTypeID(any(), any(), anyInt(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 5L, "Medical Advice" }));

			assertThat(service.getCallQualityReport(request("callTypeWise", ",\"callTypeID\":3")))
					.contains("Medical Advice");
		}

		@Test
		@DisplayName("call type wise report covers all call types when none is given")
		void callTypeWiseUnfiltered() throws Exception {
			when(crmCallReportRepo.getCallTypeWiseReport(any(), any(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 5L, "Grievance" }, new Object[] { 1L, null }));

			assertThat(service.getCallQualityReport(request("callTypeWise", ""))).contains("Grievance");
		}

		@Test
		@DisplayName("agent wise report filters by user when one is given")
		void agentWiseFilteredByUser() throws Exception {
			when(crmCallReportRepo.getAgentWiseReportByUserID(any(), any(), anyInt(), anyLong()))
					.thenReturn(List.<Object[]>of(new Object[] { 2L, "EMP1", "Asha", "K", "Rao" }));

			assertThat(service.getCallQualityReport(request("AgentWiseReport", ",\"userID\":88"))).contains("EMP1");
		}

		@Test
		@DisplayName("agent wise report covers all agents when no user is given")
		void agentWiseUnfiltered() throws Exception {
			when(crmCallReportRepo.getAgentWiseReport(any(), any(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 2L, "EMP2", "Asha", "K", "Rao" }, new Object[] { 1L, null, null, null, null }));

			assertThat(service.getCallQualityReport(request("AgentWiseReport", ""))).contains("EMP2");
		}

		@Test
		@DisplayName("skill set wise report filters by role when one is given")
		void skillSetFilteredByRole() throws Exception {
			when(crmCallReportRepo.getSkillSetWiseReportByRoleID(any(), any(), anyInt(), anyLong()))
					.thenReturn(List.<Object[]>of(new Object[] { 3L, "Nurse" }));

			assertThat(service.getCallQualityReport(request("SkillsetWiseReport", ",\"roleID\":5"))).contains("Nurse");
		}

		@Test
		@DisplayName("skill set wise report covers all roles when none is given")
		void skillSetUnfiltered() throws Exception {
			when(crmCallReportRepo.getSkillSetWiseReport(any(), any(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 3L, "Doctor" }, new Object[] { 1L, null }));

			assertThat(service.getCallQualityReport(request("SkillsetWiseReport", ""))).contains("Doctor");
		}

		@Test
		@DisplayName("date wise report reports a count per day")
		void dateWiseReport() throws Exception {
			when(crmCallReportRepo.getDateWiseReport(any(), any(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 6L, Timestamp.valueOf("2024-02-01 00:00:00") }));

			assertThat(service.getCallQualityReport(request("DateWiseReport", ""))).isNotBlank();
		}

		@Test
		@DisplayName("location wise report filters by working location when one is given")
		void locationWiseFiltered() throws Exception {
			when(crmCallReportRepo.getLocationWiseReportByLocationID(any(), any(), anyInt(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 7L, "Centre A" }));

			assertThat(service.getCallQualityReport(request("LocationWiseReport", ",\"workingLocationID\":9")))
					.contains("Centre A");
		}

		@Test
		@DisplayName("location wise report covers all locations when none is given")
		void locationWiseUnfiltered() throws Exception {
			when(crmCallReportRepo.getLocationWiseReport(any(), any(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { 7L, "Centre B" }, new Object[] { 1L, null }));

			assertThat(service.getCallQualityReport(request("LocationWiseReport", ""))).contains("Centre B");
		}

		@Test
		@DisplayName("an unrecognised search criteria echoes the request back")
		void unknownCriteriaEchoesRequest() throws Exception {
			String request = request("SomethingElse", "");

			assertThat(service.getCallQualityReport(request)).isEqualTo(request);
		}
	}

	@Nested
	@DisplayName("getDistrictWiseCallReport")
	class DistrictWiseCallReport {

		private Object[] row(String district, String gender, int count) {
			return new Object[] { 1L, district, "state", "callType", gender, BigInteger.valueOf(count) };
		}

		@Test
		@DisplayName("builds one row per district with gender split counts")
		void buildsPerDistrictRows() throws Exception {
			CallType callType = new CallType();
			ReflectionTestUtils.setField(callType, "callType", "Medical Advice");
			when(crmCallReportRepo.getCallType(anyInt())).thenReturn(List.of(callType));
			when(crmCallReportRepo.districtWiseCallVolumeReport(any(), any(), anyInt(), any()))
					.thenReturn(List.<Object[]>of(row("Bengaluru", "Female", 4), row("Bengaluru", "Male", 6),
							row("Mysuru", "Male", 2)));

			String response = service.getDistrictWiseCallReport("{\"providerServiceMapID\":12,\"districtID\":1}");

			assertThat(response).contains("Bengaluru").contains("Mysuru");
		}

		@Test
		@DisplayName("returns an empty report when the query finds nothing")
		void emptyWhenNoRows() throws Exception {
			when(crmCallReportRepo.getCallType(anyInt())).thenReturn(List.of());
			when(crmCallReportRepo.districtWiseCallVolumeReport(any(), any(), anyInt(), any())).thenReturn(List.of());

			assertThat(service.getDistrictWiseCallReport("{\"providerServiceMapID\":12}")).isNotNull();
		}
	}

	@Nested
	@DisplayName("getQualityReport")
	class QualityReport {

		private String request(int reportTypeId) {
			return "{\"reportTypeID\":" + reportTypeId + ",\"providerServiceMapID\":12}";
		}

		private List<Object[]> qaRows() {
			return List.<Object[]>of(new Object[] { Timestamp.valueOf("2024-03-01 10:00:00"), "CALL1", "AG1", "Asha",
					"Nurse", "Fever", "Viral Fever", "Medical Advice", 120, "resolved", "/rec/1.wav" });
		}

		@Test
		@DisplayName("report type 1 reports decision support calls at HAO")
		void reportTypeOne() throws Exception {
			when(crmCallReportRepo.getDSusedValidCallAtHAO(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(1))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 2 reports disconnected HAHT calls")
		void reportTypeTwo() throws Exception {
			when(crmCallReportRepo.getHAHTDisconnectedCalls(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(2))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 3 reports valid closed HAHT calls")
		void reportTypeThree() throws Exception {
			when(crmCallReportRepo.getHAHTValidCalls(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(3))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 4 reports LAHT algorithm calls")
		void reportTypeFour() throws Exception {
			when(crmCallReportRepo.getLAHTAlgorithmCalls(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(4))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 5 reports LAHT calls transferred to the medical officer")
		void reportTypeFive() throws Exception {
			when(crmCallReportRepo.getLAHTTransferCallsMO(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(5))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 6 reports other advice calls")
		void reportTypeSix() throws Exception {
			when(crmCallReportRepo.getOtherAdviceCalls(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(6))).contains("CALL1");
		}

		@Test
		@DisplayName("report type 7 reports the random pickup sample")
		void reportTypeSeven() throws Exception {
			when(crmCallReportRepo.getRandomPickup(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(qaRows());

			assertThat(service.getQualityReport(request(7))).contains("CALL1");
		}

		@Test
		@DisplayName("rows shorter than the expected width are skipped")
		void skipsShortRows() throws Exception {
			when(crmCallReportRepo.getDSusedValidCallAtHAO(anyString(), any(), any(), anyString(), anyInt()))
					.thenReturn(List.<Object[]>of(new Object[] { Timestamp.valueOf("2024-03-01 10:00:00"), "CALL2" }));

			assertThat(service.getQualityReport(request(1))).isEqualTo("[]");
		}

		@Test
		@DisplayName("an unknown report type is rejected")
		void unknownReportTypeIsRejected() {
			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.getQualityReport(request(99)))
					.withMessageContaining("Invalid Report type");
		}
	}

	@Nested
	@DisplayName("getPreviousQualityReport")
	class PreviousQualityReport {

		private CallReport callReport(String callId) {
			CallReport call = new CallReport();
			ReflectionTestUtils.setField(call, "benCallID", 55L);
			ReflectionTestUtils.setField(call, "callID", callId);
			ReflectionTestUtils.setField(call, "agentID", "AG1");
			ReflectionTestUtils.setField(call, "phoneNo", "9876500000");
			ReflectionTestUtils.setField(call, "createdDate", Timestamp.valueOf("2024-03-01 10:00:00"));
			ReflectionTestUtils.setField(call, "receivedRoleName", "Nurse");
			ReflectionTestUtils.setField(call, "remarks", "ok");
			ReflectionTestUtils.setField(call, "callTypeName", "Medical Advice");
			ReflectionTestUtils.setField(call, "recordingFilePath", "/rec/1.wav");
			DimUserReport user = new DimUserReport();
			ReflectionTestUtils.setField(user, "firstName", "Asha");
			ReflectionTestUtils.setField(call, "userReportObj", user);
			return call;
		}

		private MedHistory medHistory(long benCallId, String summary, String diagnosis) {
			MedHistory history = new MedHistory();
			history.setBenCallID(benCallId);
			history.setDiseaseSummary(summary);
			history.setSelecteDiagnosis(diagnosis);
			return history;
		}

		@Test
		@DisplayName("joins the medical history of each call and fills in the CTI call duration")
		void buildsTheReport() throws Exception {
			criteriaQueryReturns(List.of(callReport("CALL1")));
			when(crmCallReportRepo.getMedicalHistoryByBenIDs(anyList()))
					.thenReturn(new ArrayList<>(List.of(medHistory(55L, "Fever", "Viral"),
							medHistory(55L, "Cough", "Cold"))));

			CallReportsServiceImpl spy = Mockito.spy(service);
			Mockito.doReturn("{\"response\":{\"response_code\":\"1\",\"call_duration\":\"120\"}}").when(spy)
					.callUrl(anyString());

			MedHistory request = new MedHistory();
			request.setProviderServiceMapID(12);
			request.setAgentID("AG1");
			request.setRoleName("Nurse");

			assertThat(spy.getPreviousQualityReport(request)).contains("Asha");
		}

		@Test
		@DisplayName("returns an empty report when no calls match")
		void emptyWhenNoCalls() throws Exception {
			criteriaQueryReturns(List.of());

			MedHistory request = new MedHistory();
			request.setProviderServiceMapID(12);

			assertThat(service.getPreviousQualityReport(request)).isEqualTo("[]");
		}

		@Test
		@DisplayName("is reachable through report type 8")
		void reachableViaReportTypeEight() throws Exception {
			criteriaQueryReturns(List.of());

			assertThat(service.getQualityReport("{\"reportTypeID\":8,\"providerServiceMapID\":12}")).isEqualTo("[]");
		}
	}

	@Nested
	@DisplayName("getCallSummaryReport")
	class CallSummaryReport {

		@Test
		@DisplayName("summarises each matching call")
		void summarisesCalls() throws Exception {
			CallReport call = new CallReport();
			ReflectionTestUtils.setField(call, "createdDate", Timestamp.valueOf("2024-03-01 10:00:00"));
			ReflectionTestUtils.setField(call, "phoneNo", "9876500000");
			ReflectionTestUtils.setField(call, "agentID", "AG1");
			ReflectionTestUtils.setField(call, "receivedRoleName", "Nurse");
			ReflectionTestUtils.setField(call, "callTypeName", "Medical Advice");
			ReflectionTestUtils.setField(call, "providerServiceMapID", 12);
			ReflectionTestUtils.setField(call, "remarks", "resolved");
			DimUserReport user = new DimUserReport();
			ReflectionTestUtils.setField(user, "firstName", "Asha");
			ReflectionTestUtils.setField(call, "userReportObj", user);
			criteriaQueryReturns(List.of(call));

			String request = "{\"providerServiceMapID\":12,\"agentID\":\"AG1\",\"roleName\":\"Nurse\","
					+ "\"callTypeID\":3}";

			assertThat(service.getCallSummaryReport(request)).contains("Asha");
		}

		@Test
		@DisplayName("filters by call type name when no call type id is given")
		void filtersByCallTypeName() throws Exception {
			criteriaQueryReturns(List.of());

			String request = "{\"providerServiceMapID\":12,\"callTypeName\":\"Medical Advice\"}";

			assertThat(service.getCallSummaryReport(request)).isEqualTo("[]");
		}

		@Test
		@DisplayName("applies no optional filters when none are given")
		void appliesNoOptionalFilters() throws Exception {
			criteriaQueryReturns(List.of());

			assertThat(service.getCallSummaryReport("{\"providerServiceMapID\":12}")).isEqualTo("[]");
		}
	}

	@Nested
	@DisplayName("getTimeInSeconds")
	class TimeInSeconds {

		@ParameterizedTest(name = "\"{0}\" is {1} seconds")
		@CsvSource({ "'03 hours 2 mins 9 secs', 10929", "'2 mins 9 secs', 129", "'45 secs', 45", "'1 hours', 3600" })
		@DisplayName("converts a spelled out duration into seconds")
		void convertsSpelledOutDurations(String input, int expected) {
			assertThat(service.getTimeInSeconds(input)).isEqualTo(expected);
		}

		@ParameterizedTest(name = "bare \"{0}\"")
		@ValueSource(strings = { "90", "0" })
		@DisplayName("treats a bare number as a second count")
		void treatsBareNumberAsSeconds(String input) {
			assertThat(service.getTimeInSeconds(input)).isEqualTo(Integer.parseInt(input));
		}
	}

	@Test
	@DisplayName("callUrl surfaces a transport failure rather than swallowing it")
	void callUrlSurfacesTransportFailure() {
		// The service constructs its own HttpUtils, so the only observable seam is the
		// outcome of the GET. A closed local port fails fast and without DNS.
		assertThatExceptionOfType(org.springframework.web.client.ResourceAccessException.class)
				.isThrownBy(() -> service.callUrl("http://127.0.0.1:1/unreachable"));
	}
}
