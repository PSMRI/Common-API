package com.iemr.common.controller.nhmdashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.nhm_dashboard.DetailedCallReport;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.repository.nhm_dashboard.DetailedCallReportRepo;
import com.iemr.common.repository.report.CallReportRepo;

/**
 * Covers the nightly reconciliation of CTI call records against the call table.
 *
 * <p>For each CTI row the scheduler either back-fills the direction (and, where the call
 * has no type yet, resolves one from the disposition) on an existing call, or creates a
 * placeholder call for a record that never landed. Both paths and the direction and
 * call-type branches within them are driven here.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NHMDetailCallReportSchedulerTest {

	private static final String SESSION_ID = "SESSION-1";

	@Mock
	private DetailedCallReportRepo detailedCallReportRepo;
	@Mock
	private CallReportRepo callReportRepo;
	@Mock
	private IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom;

	@InjectMocks
	private NHMDetailCallReportScheduler scheduler;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(scheduler, "startCtiDataCheckFlag", true);
	}

	private DetailedCallReport ctiRecord(String phone, String orientation, String category, String disposition) {
		DetailedCallReport report = new DetailedCallReport();
		report.setSession_ID(SESSION_ID);
		report.setPHONE(phone);
		report.setOrientation_Type(orientation);
		report.setAgent_Disposition_Category(category);
		report.setAgent_Disposition(disposition);
		report.setCampaign_Name("Campaign-1");
		report.setAgent_ID(77);
		report.setCall_Duration(120);
		report.setCallStartTime(Timestamp.valueOf("2024-05-01 10:00:00"));
		report.setCallEndTime(Timestamp.valueOf("2024-05-01 10:02:00"));
		return report;
	}

	private void ctiRecordsAre(DetailedCallReport... records) {
		when(detailedCallReportRepo.findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class)))
				.thenReturn(List.of(records));
	}

	private BeneficiaryCall existingCall(Integer callTypeId) {
		BeneficiaryCall call = new BeneficiaryCall();
		ReflectionTestUtils.setField(call, "callTypeID", callTypeId);
		ReflectionTestUtils.setField(call, "calledServiceID", 12);
		return call;
	}

	/** The call-type projection the scheduler reads: callType, callTypeID, .., callGroupType. */
	private Set<Object[]> callTypeRows(String callType, Integer callTypeId, String callGroupType) {
		Set<Object[]> rows = new HashSet<>();
		rows.add(new Object[] { callType, callTypeId, "ignored", callGroupType });
		return rows;
	}

	@Nested
	@DisplayName("when the scheduler is switched off")
	class Disabled {

		@Test
		@DisplayName("nothing is read or written")
		void doesNothing() {
			ReflectionTestUtils.setField(scheduler, "startCtiDataCheckFlag", false);

			scheduler.detailedCallReport();

			verifyNoInteractions(detailedCallReportRepo, callReportRepo, iEMRCalltypeRepositoryImplCustom);
		}
	}

	@Nested
	@DisplayName("an existing call")
	class ExistingCall {

		@Test
		@DisplayName("an outbound record with no call type gets its type resolved from the disposition")
		void outboundResolvesCallType() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", "Medical_Advice", "Valid_Call"));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(null));
			when(iEMRCalltypeRepositoryImplCustom.getOutboundCallTypes(anyInt(), anyBoolean()))
					.thenReturn(callTypeRows("Valid Call", 7, "Medical Advice"));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCallWithCallType(true, SESSION_ID, "9876500000", 7);
		}

		@Test
		@DisplayName("an inbound record with no call type gets its type resolved from the disposition")
		void inboundResolvesCallType() {
			ctiRecordsAre(ctiRecord("9876500000", "INBOUND", "Medical_Advice", "Valid_Call"));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(null));
			when(iEMRCalltypeRepositoryImplCustom.getInboundCallTypes(anyInt(), anyBoolean()))
					.thenReturn(callTypeRows("Valid Call", 9, "Medical Advice"));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCallWithCallType(false, SESSION_ID, "9876500000", 9);
		}

		@Test
		@DisplayName("only the direction is written when the disposition matches no call type")
		void unmatchedDispositionWritesDirectionOnly() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", "Something_Else", "Unknown_Call"));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(null));
			when(iEMRCalltypeRepositoryImplCustom.getOutboundCallTypes(anyInt(), anyBoolean()))
					.thenReturn(callTypeRows("Valid Call", 7, "Medical Advice"));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCall(true, SESSION_ID, "9876500000");
			verify(callReportRepo, never()).updateIsOutboundForCallWithCallType(anyBoolean(), anyString(),
					anyString(), anyInt());
		}

		@Test
		@DisplayName("only the direction is written when the record carries no disposition")
		void missingDispositionWritesDirectionOnly() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(null));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCall(true, SESSION_ID, "9876500000");
			verifyNoInteractions(iEMRCalltypeRepositoryImplCustom);
		}

		@ParameterizedTest(name = "orientation {0}")
		@ValueSource(strings = { "OUTBOUND", "INBOUND" })
		@DisplayName("a call that already has a type keeps it and only has its direction written")
		void existingCallTypeIsKept(String orientation) {
			ctiRecordsAre(ctiRecord("9876500000", orientation, "Medical_Advice", "Valid_Call"));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(3));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCall("OUTBOUND".equals(orientation), SESSION_ID,
					"9876500000");
			verifyNoInteractions(iEMRCalltypeRepositoryImplCustom);
		}

		@Test
		@DisplayName("a record with no orientation is treated as inbound")
		void missingOrientationIsInbound() {
			ctiRecordsAre(ctiRecord("9876500000", null, null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(3));

			scheduler.detailedCallReport();

			verify(callReportRepo).updateIsOutboundForCall(false, SESSION_ID, "9876500000");
		}

		@Test
		@DisplayName("a leading zero is stripped from a long phone number before matching")
		void stripsLeadingZeroes() {
			ctiRecordsAre(ctiRecord("009876500000", "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), anyString()))
					.thenReturn(existingCall(3));

			scheduler.detailedCallReport();

			verify(callReportRepo).getBenCallDetailsBySessionIDAndPhone(SESSION_ID, "9876500000");
		}

		@Test
		@DisplayName("a ten digit phone number is matched as it stands")
		void tenDigitNumberIsUnchanged() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any()))
					.thenReturn(existingCall(3));

			scheduler.detailedCallReport();

			verify(callReportRepo).getBenCallDetailsBySessionIDAndPhone(SESSION_ID, "9876500000");
		}

		@Test
		@DisplayName("a record with no phone number is matched on the session alone")
		void missingPhoneNumber() {
			ctiRecordsAre(ctiRecord(null, "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any()))
					.thenReturn(existingCall(3));

			scheduler.detailedCallReport();

			verify(callReportRepo).getBenCallDetailsBySessionIDAndPhone(SESSION_ID, null);
		}
	}

	@Nested
	@DisplayName("a missing call")
	class MissingCall {

		@Test
		@DisplayName("a placeholder call is created from the CTI record")
		void createsAPlaceholderCall() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any())).thenReturn(null);

			scheduler.detailedCallReport();

			ArgumentCaptor<BeneficiaryCall> captor = ArgumentCaptor.forClass(BeneficiaryCall.class);
			verify(callReportRepo).save(captor.capture());
			BeneficiaryCall created = captor.getValue();
			assertThat(created.getCallID()).isEqualTo(SESSION_ID);
			assertThat(created.getPhoneNo()).isEqualTo("9876500000");
			assertThat(created.getCalledServiceID()).isNull();
			assertThat(created.getRemarks()).isEqualTo("missed records - Failure");
			assertThat(created.getIs1097()).isTrue();
			assertThat(created.getReceivedRoleName()).isEqualTo("Campaign-1");
			assertThat(created.getAgentID()).isEqualTo("77");
			assertThat(created.getIsOutbound()).isTrue();
			assertThat(created.getCallDuration()).isEqualTo("120");
			assertThat(created.getCreatedBy()).isEqualTo("Admin");
			assertThat(created.getCallTime()).isEqualTo(Timestamp.valueOf("2024-05-01 10:00:00"));
			assertThat(created.getCallEndTime()).isEqualTo(Timestamp.valueOf("2024-05-01 10:02:00"));
		}

		@Test
		@DisplayName("an inbound placeholder is marked as not outbound")
		void inboundPlaceholder() {
			ctiRecordsAre(ctiRecord("9876500000", "INBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any())).thenReturn(null);

			scheduler.detailedCallReport();

			ArgumentCaptor<BeneficiaryCall> captor = ArgumentCaptor.forClass(BeneficiaryCall.class);
			verify(callReportRepo).save(captor.capture());
			assertThat(captor.getValue().getIsOutbound()).isFalse();
		}

		@Test
		@DisplayName("a leading zero is stripped from the placeholder's phone number")
		void stripsLeadingZeroesOnThePlaceholder() {
			ctiRecordsAre(ctiRecord("009876500000", "OUTBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any())).thenReturn(null);

			scheduler.detailedCallReport();

			ArgumentCaptor<BeneficiaryCall> captor = ArgumentCaptor.forClass(BeneficiaryCall.class);
			verify(callReportRepo).save(captor.capture());
			assertThat(captor.getValue().getPhoneNo()).isEqualTo("9876500000");
		}
	}

	@Nested
	@DisplayName("robustness")
	class Robustness {

		@Test
		@DisplayName("nothing is written when there are no CTI records for the day")
		void noRecordsForTheDay() {
			ctiRecordsAre();

			scheduler.detailedCallReport();

			verify(callReportRepo, never()).save(any(BeneficiaryCall.class));
		}

		@Test
		@DisplayName("every record is reconciled even when there are several")
		void reconcilesEveryRecord() {
			ctiRecordsAre(ctiRecord("9876500000", "OUTBOUND", null, null),
					ctiRecord("9876500001", "INBOUND", null, null));
			when(callReportRepo.getBenCallDetailsBySessionIDAndPhone(anyString(), any())).thenReturn(null);

			scheduler.detailedCallReport();

			verify(callReportRepo, org.mockito.Mockito.times(2)).save(any(BeneficiaryCall.class));
		}

		@Test
		@DisplayName("a repository failure is contained rather than propagated")
		void containsRepositoryFailures() {
			when(detailedCallReportRepo.findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class)))
					.thenThrow(new IllegalStateException("db down"));

			scheduler.detailedCallReport();

			verify(callReportRepo, never()).save(any(BeneficiaryCall.class));
		}
	}
}
