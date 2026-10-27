package com.iemr.common.service.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.data.institute.Institute;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.data.sms.SMSNotification;
import com.iemr.common.data.sms.SMSParameters;
import com.iemr.common.data.sms.SMSParametersMap;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.data.users.User;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.sms.SMSMapper;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.repository.helpline104history.PrescribedDrugRepository;
import com.iemr.common.repository.institute.InstituteRepository;
import com.iemr.common.repository.location.LocationDistrictBlockRepository;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.repository.mctshistory.OutboundHistoryRepository;
import com.iemr.common.repository.sms.SMSNotificationRepository;
import com.iemr.common.repository.sms.SMSParameterMapRepository;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.users.IEMRUserRepositoryCustom;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.service.beneficiary.IEMRSearchUserService;

/**
 * Covers the template-substitution path that turns an SMS request into a stored notification.
 *
 * <p>{@code prepareSMS} walks the parameter map of a template and resolves each placeholder
 * from a different data source depending on the parameter's declared source type. Each of
 * those branches is driven here, along with the phone-number resolution precedence and the
 * video-consultation and prescription variants that {@code sendSMS} routes to.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SMSServiceImplPrepareSMSTest {

	private static final Integer TEMPLATE_ID = 11;
	private static final String AUTH_TOKEN = "auth-token";

	@Mock
	private SMSMapper smsMapper;
	@Mock
	private SMSTemplateRepository smsTemplateRepository;
	@Mock
	private SMSParameterMapRepository smsParameterMapRepository;
	@Mock
	private SMSNotificationRepository smsNotificationRepository;
	@Mock
	private VideoCallParameterRepository videoCallParameterRepository;
	@Mock
	private InstituteRepository instituteRepository;
	@Mock
	private IEMRUserRepositoryCustom userRepository;
	@Mock
	private FeedbackRepository feedbackReporsitory;
	@Mock
	private IEMRSearchUserService searchBeneficiary;
	@Mock
	private PrescribedDrugRepository prescribedDrugRepository;
	@Mock
	private OutboundHistoryRepository outboundHistoryRepository;
	@Mock
	private LocationStateRepository stateRepository;
	@Mock
	private LocationDistrictRepository districtRepository;
	@Mock
	private LocationDistrictBlockRepository blockRepository;

	@InjectMocks
	private SMSServiceImpl service;

	@BeforeEach
	void setUp() {
		// SMSNotification.toString() serialises through OutputMapper's static Gson builders,
		// which are created by its constructor.
		new com.iemr.common.utils.mapper.OutputMapper();
		// The notification repository hands back whatever it is asked to save so the
		// assembled message can be inspected.
		when(smsNotificationRepository.save(any(SMSNotification.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	private SMSTemplate template(String name, String body) {
		SMSTemplate template = new SMSTemplate();
		template.setSmsTemplateID(TEMPLATE_ID);
		template.setSmsTemplateName(name);
		template.setSmsTemplate(body);
		template.setSmsTypeID(1);
		return template;
	}

	/** A parameter map entry declaring one placeholder and where its value comes from. */
	private SMSParametersMap parameter(String placeholder, String sourceType, String className, String dataName) {
		SMSParameters parameters = new SMSParameters();
		parameters.setSmsParameterType(sourceType);
		parameters.setDataClassName(className);
		parameters.setDataName(dataName);

		SMSParametersMap map = new SMSParametersMap();
		map.setSmsParameterName(placeholder);
		map.setSmsParameter(parameters);
		map.setSmsTemplateID(TEMPLATE_ID);
		return map;
	}

	private SMSRequest request() {
		SMSRequest request = new SMSRequest();
		request.setSmsTemplateID(TEMPLATE_ID);
		request.setCreatedBy("tester");
		request.setUserID(7L);
		request.setIs1097(false);
		request.setIsBloodBankSMS(false);
		return request;
	}

	private void withTemplate(SMSTemplate template, SMSParametersMap... parameters) {
		when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID)).thenReturn(template);
		when(smsParameterMapRepository.findSMSParametersMapBySmsTemplateID(TEMPLATE_ID))
				.thenReturn(new ArrayList<>(List.of(parameters)));
	}

	@Nested
	@DisplayName("sendSMS with an explicit message body")
	class ExplicitBody {

		@Test
		@DisplayName("uses the supplied text verbatim without consulting a template")
		void usesSuppliedText() throws Exception {
			SMSRequest request = request();
			request.setSmsText("A fully formed message");
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID)).thenReturn(null);

			assertThat(service.sendSMS(List.of(request), AUTH_TOKEN)).contains("A fully formed message");
		}

		@Test
		@DisplayName("marks the notification as not yet sent and stamps the trigger time")
		void marksNotificationAsPending() throws Exception {
			SMSRequest request = request();
			request.setSmsText("Pending message");
			request.setBeneficiaryRegID(4242L);
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID)).thenReturn(null);

			SMSNotification saved = capture(request);

			assertThat(saved.getSmsStatus()).isEqualTo(SMSNotification.NOT_SENT);
			assertThat(saved.getCreatedBy()).isEqualTo("tester");
			assertThat(saved.getBeneficiaryRegID()).isEqualTo(4242L);
			assertThat(saved.getReceivingUserID()).isEqualTo(7L);
			assertThat(saved.getSmsTriggerDate()).isNotNull();
		}
	}

	/** Runs one request through sendSMS and returns the notification that was stored. */
	private SMSNotification capture(SMSRequest request) throws Exception {
		List<SMSNotification> stored = new ArrayList<>();
		when(smsNotificationRepository.save(any(SMSNotification.class))).thenAnswer(invocation -> {
			SMSNotification notification = invocation.getArgument(0);
			stored.add(notification);
			return notification;
		});
		service.sendSMS(List.of(request), AUTH_TOKEN);
		assertThat(stored).isNotEmpty();
		return stored.get(stored.size() - 1);
	}

	@Nested
	@DisplayName("placeholder substitution by parameter source")
	class PlaceholderSubstitution {

		@Test
		@DisplayName("a Beneficiary parameter is filled from the looked up beneficiary")
		void beneficiarySource() throws Exception {
			BeneficiaryModel beneficiary = new BeneficiaryModel();
			beneficiary.setFirstName("Latha");
			beneficiary.setLastName("Devi");
			when(searchBeneficiary.userExitsCheckWithId(anyLong(), anyString(), any()))
					.thenReturn(new ArrayList<>(List.of(beneficiary)));
			withTemplate(template("Reminder", "Hello $$NAME$$"),
					parameter("NAME", "Beneficiary", "className", "name"));

			SMSRequest request = request();
			request.setBeneficiaryRegID(4242L);

			assertThat(capture(request).getSms()).isEqualTo("Hello Latha Devi");
		}

		@Test
		@DisplayName("a User parameter is filled from the receiving user record")
		void userSource() throws Exception {
			User user = new User();
			user.setFirstName("Asha");
			when(userRepository.findByUserID(7L)).thenReturn(user);
			withTemplate(template("Reminder", "Agent $$AGENT$$"),
					parameter("AGENT", "User", "className", "FirstName"));

			assertThat(capture(request()).getSms()).isEqualTo("Agent Asha");
		}

		@Test
		@DisplayName("an Institute parameter is filled from the location masters for a 1097 request")
		void instituteSourceFor1097() throws Exception {
			States state = new States();
			state.setStateName("Karnataka");
			when(stateRepository.findByStateID(any(Integer.class))).thenReturn(state);
			when(instituteRepository.findByInstitutionID(any(Integer.class))).thenReturn(new Institute(9, "PHC Anekal"));
			withTemplate(template("Reminder", "State $$STATE$$"),
					parameter("STATE", "Institute", "className", "StateName"));

			SMSRequest request = request();
			request.setIs1097(true);
			request.setStateID(1);
			request.setInstituteID(9);

			assertThat(capture(request).getSms()).isEqualTo("State Karnataka");
		}

		@Test
		@DisplayName("a district Institute parameter reads the district master")
		void instituteSourceDistrict() throws Exception {
			Districts district = new Districts();
			district.setDistrictName("Bengaluru");
			when(districtRepository.findByDistrictID(any(Integer.class))).thenReturn(district);
			when(instituteRepository.findByInstitutionID(any(Integer.class))).thenReturn(new Institute(9, "PHC Anekal"));
			withTemplate(template("Reminder", "District $$DISTRICT$$"),
					parameter("DISTRICT", "Institute", "className", "DistrictName"));

			SMSRequest request = request();
			request.setIs1097(true);
			request.setDistrictID(2);
			request.setInstituteID(9);

			assertThat(capture(request).getSms()).isEqualTo("District Bengaluru");
		}

		@Test
		@DisplayName("a block Institute parameter reads the block master")
		void instituteSourceBlock() throws Exception {
			DistrictBlock block = new DistrictBlock();
			block.setBlockName("Anekal");
			when(blockRepository.findByBlockID(any(Integer.class))).thenReturn(block);
			when(instituteRepository.findByInstitutionID(any(Integer.class))).thenReturn(new Institute(9, "PHC Anekal"));
			withTemplate(template("Reminder", "Block $$BLOCK$$"),
					parameter("BLOCK", "Institute", "className", "BlockName"));

			SMSRequest request = request();
			request.setIs1097(true);
			request.setBlockID(3);
			request.setInstituteID(9);

			assertThat(capture(request).getSms()).isEqualTo("Block Anekal");
		}

		@Test
		@DisplayName("a 104 appointment parameter is filled straight from the request")
		void uptsuSource() throws Exception {
			withTemplate(template("Reminder", "Visit $$FACILITY$$"),
					parameter("FACILITY", "104 appointment", "className", "facilityName"));

			SMSRequest request = request();
			request.setFacilityName("PHC Anekal");

			assertThat(capture(request).getSms()).isEqualTo("Visit PHC Anekal");
		}

		@Test
		@DisplayName("an unrecognised parameter source leaves the placeholder empty")
		void unknownSource() throws Exception {
			withTemplate(template("Reminder", "Value [$$THING$$]"),
					parameter("THING", "Not A Known Source", "className", "whatever"));

			assertThat(capture(request()).getSms()).isEqualTo("Value []");
		}

		@Test
		@DisplayName("a template with no parameters is sent as written")
		void noParameters() throws Exception {
			withTemplate(template("Reminder", "A static message"));

			assertThat(capture(request()).getSms()).isEqualTo("A static message");
		}

		@Test
		@DisplayName("the message is left empty when the template cannot be found")
		void missingTemplate() throws Exception {
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID)).thenReturn(null);
			when(smsParameterMapRepository.findSMSParametersMapBySmsTemplateID(TEMPLATE_ID))
					.thenReturn(new ArrayList<>());

			assertThat(capture(request()).getSms()).isEmpty();
		}

		@Test
		@DisplayName("a medical officer advice is appended for an advice template")
		void appendsMedicalOfficerAdvice() throws Exception {
			SMSTemplate template = template("Advice", "Your advice: ");
			template.setSmsTypeID(15);
			withTemplate(template);

			SMSRequest request = request();
			request.setMoAdvice("take rest");

			assertThat(capture(request).getSms()).isEqualTo("Your advice: take rest");
		}
	}

	@Nested
	@DisplayName("phone number resolution")
	class PhoneNumberResolution {

		@Test
		@DisplayName("the SMS_PHONE_NO placeholder sets the recipient rather than the body")
		void phonePlaceholderSetsRecipient() throws Exception {
			withTemplate(template("Reminder", "Body stays put"),
					parameter("SMS_PHONE_NO", "104 appointment", "className", "facilityName"));

			SMSRequest request = request();
			request.setFacilityName("9876500000");

			SMSNotification saved = capture(request);
			assertThat(saved.getPhoneNo()).isEqualTo("9876500000");
			assertThat(saved.getSms()).isEqualTo("Body stays put");
		}

		@Test
		@DisplayName("a nodal number overrides the resolved recipient")
		void nodalNumberWins() throws Exception {
			withTemplate(template("Reminder", "Body"),
					parameter("SMS_PHONE_NO", "104 appointment", "className", "facilityName"));

			SMSRequest request = request();
			request.setFacilityName("9876500000");
			request.setNodalNumber("9000000000");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9000000000");
		}

		@Test
		@DisplayName("an alternate number set on the request takes precedence")
		void alternateNumberWins() throws Exception {
			withTemplate(template("Reminder", "Body"));

			SMSRequest request = request();
			request.setAlternateNo("9111111111");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9111111111");
		}

		@Test
		@DisplayName("the beneficiary number is the last word on the recipient")
		void beneficiaryNumberWinsOverall() throws Exception {
			withTemplate(template("Reminder", "Body"));

			SMSRequest request = request();
			request.setAlternateNo("9111111111");
			request.setFacilityPhoneNo("9222222222");
			request.setBenPhoneNo("9333333333");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9333333333");
		}

		@Test
		@DisplayName("a facility number overrides an alternate number")
		void facilityNumberOverridesAlternate() throws Exception {
			withTemplate(template("Reminder", "Body"));

			SMSRequest request = request();
			request.setAlternateNo("9111111111");
			request.setFacilityPhoneNo("9222222222");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9222222222");
		}
	}

	@Nested
	@DisplayName("video consultation messages")
	class VideoConsultation {

		private VideoCallParameters videoCall() {
			VideoCallParameters parameters = new VideoCallParameters();
			parameters.setMeetingLink("https://meet.example/abc");
			parameters.setDateOfCall(Timestamp.valueOf("2024-05-01 10:30:00"));
			parameters.setCallerPhoneNumber("9876500000");
			return parameters;
		}

		@Test
		@DisplayName("substitutes the meeting link into a video consultation template")
		void substitutesMeetingLink() throws Exception {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(videoCall());
			withTemplate(template("Video Consultation", "Join $$LINK$$"),
					parameter("LINK", "VideoCall", "className", "videoConsultationLink"));

			SMSRequest request = request();
			request.setSmsAdvice("https://meet.example/abc");

			assertThat(capture(request).getSms()).isEqualTo("Join https://meet.example/abc");
		}

		@Test
		@DisplayName("fails when the request carries no meeting link")
		void failsWithoutMeetingLink() {
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID))
					.thenReturn(template("Video Consultation", "Join $$LINK$$"));

			assertThatExceptionOfType(Exception.class)
					.isThrownBy(() -> service.sendSMS(List.of(request()), AUTH_TOKEN))
					.withMessageContaining("Meeting link is missing");
		}

		@Test
		@DisplayName("fails when the meeting link is unknown")
		void failsForUnknownMeetingLink() {
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID))
					.thenReturn(template("Video Consultation", "Join $$LINK$$"));
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(null);

			SMSRequest request = request();
			request.setSmsAdvice("https://meet.example/missing");

			assertThatExceptionOfType(Exception.class)
					.isThrownBy(() -> service.sendSMS(List.of(request), AUTH_TOKEN))
					.withMessageContaining("Video Call Parameters not found");
		}

		@Test
		@DisplayName("prepareSMSWithVideoCall builds the message for a known link")
		void prepareWithVideoCallForKnownLink() throws Exception {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(videoCall());
			withTemplate(template("Video Consultation", "Join $$LINK$$"),
					parameter("LINK", "VideoCall", "className", "videoConsultationLink"));

			SMSNotification sms = service.prepareSMSWithVideoCall(request(), AUTH_TOKEN, "https://meet.example/abc");

			assertThat(sms.getSms()).isEqualTo("Join https://meet.example/abc");
		}

		@Test
		@DisplayName("prepareSMSWithVideoCall rejects an unknown link")
		void prepareWithVideoCallRejectsUnknownLink() {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(null);

			assertThatExceptionOfType(Exception.class)
					.isThrownBy(() -> service.prepareSMSWithVideoCall(request(), AUTH_TOKEN, "https://nope"))
					.withMessageContaining("Video Call Parameters not found");
		}

		@Test
		@DisplayName("the beneficiary number wins over the resolved video call number")
		void beneficiaryNumberWins() throws Exception {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(videoCall());
			withTemplate(template("Video Consultation", "Join"),
					parameter("SMS_PHONE_NO", "VideoCall", "className", "phoneNo"));

			SMSRequest request = request();
			request.setSmsAdvice("https://meet.example/abc");
			request.setBenPhoneNo("9444444444");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9444444444");
		}

		@Test
		@DisplayName("the video call number is used when the request has none")
		void videoCallNumberUsedAsFallback() throws Exception {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(videoCall());
			withTemplate(template("Video Consultation", "Join"),
					parameter("SMS_PHONE_NO", "VideoCall", "className", "phoneNo"));

			SMSRequest request = request();
			request.setSmsAdvice("https://meet.example/abc");

			assertThat(capture(request).getPhoneNo()).isEqualTo("9876500000");
		}

		@Test
		@DisplayName("an empty video consultation template yields an empty message")
		void missingVideoTemplate() throws Exception {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(videoCall());
			when(smsTemplateRepository.findBySmsTemplateID(TEMPLATE_ID))
					.thenReturn(template("Video Consultation", "x"), (SMSTemplate) null);

			SMSRequest request = request();
			request.setSmsAdvice("https://meet.example/abc");

			assertThat(capture(request).getSms()).isEmpty();
		}
	}

	@Nested
	@DisplayName("getVideoCallData")
	class GetVideoCallData {

		private VideoCallParameters videoCall() {
			VideoCallParameters parameters = new VideoCallParameters();
			parameters.setMeetingLink("https://meet.example/abc");
			parameters.setDateOfCall(Timestamp.valueOf("2024-05-01 14:30:00"));
			parameters.setCallerPhoneNumber("9876500000");
			return parameters;
		}

		@Test
		@DisplayName("returns the meeting link")
		void returnsMeetingLink() throws Exception {
			assertThat(service.getVideoCallData("videoConsultationLink", videoCall()))
					.isEqualTo("https://meet.example/abc");
		}

		@Test
		@DisplayName("formats the consultation date for display")
		void formatsConsultationDate() throws Exception {
			assertThat(service.getVideoCallData("consultationDate", videoCall()))
					.isEqualToIgnoringCase("01-05-2024 2:30 PM");
		}

		@Test
		@DisplayName("returns the caller phone number")
		void returnsPhoneNumber() throws Exception {
			assertThat(service.getVideoCallData("phoneNo", videoCall())).isEqualTo("9876500000");
		}

		@ParameterizedTest(name = "unset {0} yields an empty value")
		@ValueSource(strings = { "videoConsultationLink", "phoneNo", "consultationDate" })
		@DisplayName("an unset field yields an empty value")
		void unsetFieldsAreEmpty(String field) throws Exception {
			assertThat(service.getVideoCallData(field, new VideoCallParameters())).isEmpty();
		}

		@Test
		@DisplayName("an unrecognised field with no matching getter yields an empty value")
		void unknownFieldIsEmpty() throws Exception {
			assertThat(service.getVideoCallData("noSuchThing", videoCall())).isEmpty();
		}

		@Test
		@DisplayName("an unrecognised field falls back to a matching getter")
		void unknownFieldFallsBackToGetter() throws Exception {
			VideoCallParameters parameters = videoCall();
			parameters.setAgentName("Asha K");

			assertThat(service.getVideoCallData("agentName", parameters)).isEqualTo("Asha K");
		}
	}

	@Nested
	@DisplayName("sendSMS over several requests")
	class MultipleRequests {

		@Test
		@DisplayName("builds one notification per request")
		void oneNotificationPerRequest() throws Exception {
			withTemplate(template("Reminder", "Static body"));

			String response = service.sendSMS(List.of(request(), request()), AUTH_TOKEN);

			assertThat(response).isNotBlank();
		}

		@Test
		@DisplayName("an empty request list produces an empty result")
		void emptyRequestList() throws Exception {
			assertThat(service.sendSMS(new ArrayList<>(), AUTH_TOKEN)).isEqualTo("[]");
		}
	}

	@Nested
	@DisplayName("getVideoCallParameters")
	class GetVideoCallParameters {

		@Test
		@DisplayName("looks the parameters up by meeting link")
		void looksUpByMeetingLink() {
			VideoCallParameters parameters = new VideoCallParameters();
			when(videoCallParameterRepository.findByMeetingLink("https://meet.example/abc")).thenReturn(parameters);

			assertThat(service.getVideoCallParameters("https://meet.example/abc")).isSameAs(parameters);
		}

		@Test
		@DisplayName("returns nothing for an unknown meeting link")
		void unknownMeetingLink() {
			when(videoCallParameterRepository.findByMeetingLink(anyString())).thenReturn(null);

			assertThat(service.getVideoCallParameters("https://meet.example/missing")).isNull();
		}
	}
}
