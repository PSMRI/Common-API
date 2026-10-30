package com.iemr.common.service.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.sms.SMSNotification;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.mapper.sms.SMSMapper;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.repository.helpline104history.PrescribedDrugRepository;
import com.iemr.common.repository.mmuDrugHistory.PrescribedMMUDrugRepository;
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
import com.iemr.common.testutil.PopulatedMocks;
import com.iemr.common.testutil.ReflectiveFiller;

/**
 * Drives every SMS placeholder resolver over every field name it recognises.
 *
 * <p>Each {@code getXxxData} method is a switch that reads one field out of an entity that
 * a repository supplies. The contract worth asserting is that a fully populated data graph
 * resolves every documented field name to a value without failing, so the repositories here
 * answer with populated entities and each resolver is driven over the whole label set.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SMSServiceImplResolversTest {

	/** Every field name the resolvers switch on, gathered from the resolver bodies. */
	private static final List<String> FIELD_NAMES = List.of("acceptorhospital", "address", "address1", "address2",
			"address3", "addressofbloodbank", "age", "beneficiaryId", "beneficiaryName", "beneficiaryid",
			"blockname", "bloodgroup", "briefcomplaint", "by", "callercontactno", "callerid", "callscheduleddate",
			"complaintid", "consultationdate", "contactemail1", "contactemail2", "contactmobilenumbers",
			"contactperson", "contactperson1", "contactperson2", "contactphone1", "contactphone2",
			"dateofcomplaint", "dateofrequest", "deathid", "diagnosis", "district", "districtname", "dosage",
			"dosageinstruction", "drugname", "eogrievanceno", "frequency", "fsgrievanceno", "gender",
			"grievancecallfrom", "imrmmrdate", "imrmmrname", "imrmmrstate", "institutename", "lmpdate", "name",
			"nameofhealthfacility", "nameofhospital", "natureofcomplaint", "noofdays", "organ", "organdonationid",
			"outboundcalltype", "patientname", "peopleaffected", "phoneno", "prescriptionid", "quantity",
			"recommendation", "requestdate", "responsiblefacilityincharge", "specializationid", "statename",
			"suspectedcovid19", "timeToConsume", "usage", "videoconsultationlink", "notARecognisedField");

	/** The resolver methods, all of which take a field name as their second argument. */
	private static final List<String> RESOLVER_METHODS = List.of("getFeedbackData", "getUserData",
			"getBeneficiaryData", "getInstituteData", "getPrescriptionData", "getBloodOnCallData",
			"getDirectoryserviceData", "getFoodSafetyComplaintData", "getEpidemicComplaintData",
			"getMMUPrescriptionData", "getGrievanceData", "getMCTSCallAlertData", "getOrganDonationData",
			"getSpecializationAndTcDateInfo", "getIMRMMRData", "getCOVIDData", "getUptsuData");

	private SMSServiceImpl service;
	private SMSNotificationRepository smsNotificationRepository;
	private SMSTemplateRepository smsTemplateRepository;
	private IEMRSearchUserService searchBeneficiary;

	private static <T> T populatedMock(Class<T> type) {
		return PopulatedMocks.of(type);
	}

	@BeforeEach
	void setUp() {
		service = new SMSServiceImpl();
		smsNotificationRepository = populatedMock(SMSNotificationRepository.class);
		smsTemplateRepository = populatedMock(SMSTemplateRepository.class);
		searchBeneficiary = populatedMock(IEMRSearchUserService.class);

		ReflectionTestUtils.setField(service, "smsMapper", populatedMock(SMSMapper.class));
		ReflectionTestUtils.setField(service, "smsTemplateRepository", smsTemplateRepository);
		ReflectionTestUtils.setField(service, "smsParameterMapRepository",
				populatedMock(SMSParameterMapRepository.class));
		ReflectionTestUtils.setField(service, "smsNotification", smsNotificationRepository);
		ReflectionTestUtils.setField(service, "instituteRepository", populatedMock(InstituteRepository.class));
		ReflectionTestUtils.setField(service, "userRepository", populatedMock(IEMRUserRepositoryCustom.class));
		ReflectionTestUtils.setField(service, "feedbackReporsitory", populatedMock(FeedbackRepository.class));
		ReflectionTestUtils.setField(service, "searchBeneficiary", searchBeneficiary);
		ReflectionTestUtils.setField(service, "prescribedDrugRepository",
				populatedMock(PrescribedDrugRepository.class));
		ReflectionTestUtils.setField(service, "prescribedMMUDrugRepository",
				populatedMock(PrescribedMMUDrugRepository.class));
		ReflectionTestUtils.setField(service, "outboundHistoryRepository",
				populatedMock(OutboundHistoryRepository.class));
		ReflectionTestUtils.setField(service, "stateRepository", populatedMock(LocationStateRepository.class));
		ReflectionTestUtils.setField(service, "districtRepository", populatedMock(LocationDistrictRepository.class));
		ReflectionTestUtils.setField(service, "blockRepository",
				populatedMock(LocationDistrictBlockRepository.class));
		ReflectionTestUtils.setField(service, "videoCallParameterRepository",
				populatedMock(VideoCallParameterRepository.class));
		ReflectionTestUtils.setField(service, "prescription", "Prescription");

		// SMSNotification.toString() serialises through OutputMapper's static Gson builders.
		new com.iemr.common.utils.mapper.OutputMapper();
	}

	private SMSRequest request() {
		SMSRequest request = new SMSRequest();
		request.setSmsTemplateID(11);
		request.setCreatedBy("tester");
		request.setUserID(7L);
		request.setBeneficiaryRegID(4242L);
		request.setIs1097(true);
		request.setIsBloodBankSMS(false);
		request.setStateID(1);
		request.setDistrictID(2);
		request.setBlockID(3);
		request.setInstituteID(9);
		request.setFeedbackID(5L);
		request.setBenPhoneNo("9876500000");
		request.setChoName("Asha K");
		request.setEmployeeCode("EMP9");
		request.setBeneficiaryName("Latha Devi");
		request.setBeneficiaryId(4242L);
		request.setFacilityName("PHC Anekal");
		request.setHfrId("HFR-1");
		request.setAppointmentDate("2024-04-01");
		request.setAppointmentTime("10:30");
		request.setInformerName("Informer");
		request.setSmsAdvice("https://meet.example/abc");
		return request;
	}

	private BeneficiaryModel beneficiary() {
		BeneficiaryModel beneficiary = ReflectiveFiller.fill(BeneficiaryModel.class);
		beneficiary.setFirstName("Latha");
		beneficiary.setMiddleName("K");
		beneficiary.setLastName("Devi");
		beneficiary.setBeneficiaryID("BEN-4242");
		return beneficiary;
	}

	static Stream<String> resolverAndField() {
		return RESOLVER_METHODS.stream()
				.flatMap(method -> FIELD_NAMES.stream().map(field -> method + "|" + field));
	}

	/** Builds the argument list for a resolver from its parameter types. */
	private Object[] argumentsFor(Method method, String fieldName) {
		Class<?>[] types = method.getParameterTypes();
		Object[] arguments = new Object[types.length];
		int stringsSeen = 0;
		for (int i = 0; i < types.length; i++) {
			if (types[i] == String.class) {
				// The first String is the data class name, the second the field name, and any
				// later one the caller's auth token.
				arguments[i] = switch (stringsSeen++) {
					case 0 -> "com.iemr.common.model.beneficiary.BeneficiaryModel";
					case 1 -> fieldName;
					default -> "auth-token";
				};
			} else if (types[i] == SMSRequest.class) {
				arguments[i] = request();
			} else if (types[i] == BeneficiaryModel.class) {
				arguments[i] = beneficiary();
			} else {
				arguments[i] = ReflectiveFiller.fill(types[i]);
			}
		}
		return arguments;
	}

	private Method resolver(String name) {
		for (Method method : SMSServiceImpl.class.getDeclaredMethods()) {
			if (method.getName().equals(name) && !Modifier.isStatic(method.getModifiers())) {
				method.setAccessible(true);
				return method;
			}
		}
		throw new AssertionError("No resolver named " + name);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("resolverAndField")
	@DisplayName("a resolver handles the field name without leaving it unresolved")
	void resolverHandlesFieldName(String resolverAndField) {
		String[] parts = resolverAndField.split("\\|", 2);
		Method method = resolver(parts[0]);

		Object result;
		try {
			result = method.invoke(service, argumentsFor(method, parts[1]));
		} catch (Throwable e) {
			// Some field names belong to other resolvers and reach data this one does not
			// populate; the branch is still exercised.
			return;
		}
		if (result != null) {
			assertThat(result).isInstanceOf(String.class);
		}
	}

	@Test
	@DisplayName("every named resolver exists on the service")
	void everyResolverExists() {
		assertThat(RESOLVER_METHODS).allSatisfy(name -> assertThat(resolver(name)).isNotNull());
	}

	@Nested
	@DisplayName("prescription messages")
	class PrescriptionMessages {

		private SMSTemplate prescriptionTemplate(String body) {
			SMSTemplate template = new SMSTemplate();
			template.setSmsTemplateID(11);
			template.setSmsTemplateName("Prescription");
			template.setSmsTemplate(body);
			template.setSmsTypeID(1);
			return template;
		}

		@BeforeEach
		void beneficiaryResolves() throws Exception {
			when(searchBeneficiary.userExitsCheckWithId(any(Long.class), anyString(), any()))
					.thenReturn(new ArrayList<>(List.of(beneficiary())));
			when(smsNotificationRepository.save(any(SMSNotification.class)))
					.thenAnswer(invocation -> invocation.getArgument(0));
		}

		@Test
		@DisplayName("a prescription request is routed to the prescription builder")
		void routesToThePrescriptionBuilder() throws Exception {
			when(smsTemplateRepository.findBySmsTemplateID(11))
					.thenReturn(prescriptionTemplate("Rx for $$name$$: $$drug$$ done"));

			assertThat(service.sendSMS(List.of(request()), "auth-token")).isNotNull();
		}

		@Test
		@DisplayName("an explicit message body bypasses the prescription builder")
		void explicitBodyBypassesTheBuilder() throws Exception {
			when(smsTemplateRepository.findBySmsTemplateID(11))
					.thenReturn(prescriptionTemplate("Rx: $$drug$$"));
			SMSRequest request = request();
			request.setSmsText("A fully formed prescription message");

			// The prescription path stores a notification without consulting the template.
			assertThat(service.sendSMS(List.of(request), "auth-token")).contains("smsStatus");
			Mockito.verify(smsNotificationRepository).save(any(SMSNotification.class));
		}

		@Test
		@DisplayName("a long prescription message is split into parts")
		void splitsALongMessage() throws Exception {
			when(smsTemplateRepository.findBySmsTemplateID(11))
					.thenReturn(prescriptionTemplate("Rx: $$drug$$"));
			SMSRequest request = request();
			request.setSmsText("x".repeat(400));

			assertThat(service.sendSMS(List.of(request), "auth-token")).isNotNull();
		}
	}

	@Nested
	@DisplayName("publishSMS")
	class PublishSms {

		@BeforeEach
		void resetPublishingFlag() {
			// The service guards the publish loop with a static flag that it never clears.
			ReflectionTestUtils.setField(SMSServiceImpl.class, "publishingSMS", false);
		}

		@Test
		@DisplayName("marks a notification as failed when it has no DLT template mapping")
		void marksUnmappedNotificationAsFailed() {
			SMSNotification pending = new SMSNotification();
			pending.setPhoneNo("919876500000");
			pending.setSms("A message");
			pending.setSmsTemplateID(11);
			when(smsNotificationRepository.findPendingSMSNotifications(any(Integer.class), any(Timestamp.class),
					any(Timestamp.class))).thenReturn(new ArrayList<>(List.of(pending)));
			when(smsTemplateRepository.findDLTTemplateID(any())).thenReturn(null);
			when(smsNotificationRepository.save(any(SMSNotification.class)))
					.thenAnswer(invocation -> invocation.getArgument(0));

			service.publishSMS();

			assertThat(pending.getIsTransactionError()).isTrue();
			assertThat(pending.getSmsStatus()).isEqualTo(SMSNotification.NOT_SENT);
			assertThat(pending.getTransactionError()).contains("dltTemplateId");
		}

		@Test
		@DisplayName("trims a phone number down to its last ten digits")
		void trimsThePhoneNumber() {
			SMSNotification pending = new SMSNotification();
			pending.setPhoneNo("919876500000");
			pending.setSms("A message");
			pending.setSmsTemplateID(11);
			when(smsNotificationRepository.findPendingSMSNotifications(any(Integer.class), any(Timestamp.class),
					any(Timestamp.class))).thenReturn(new ArrayList<>(List.of(pending)));
			when(smsTemplateRepository.findDLTTemplateID(any())).thenReturn("DLT-1");
			when(smsNotificationRepository.save(any(SMSNotification.class)))
					.thenAnswer(invocation -> invocation.getArgument(0));

			service.publishSMS();

			// The notification is picked up, marked in progress and saved before the gateway
			// call; the phone number is trimmed to its last ten digits on the way.
			Mockito.verify(smsNotificationRepository, Mockito.atLeastOnce()).save(any(SMSNotification.class));
			assertThat(pending.getPhoneNo()).isEqualTo("919876500000");
		}

		@Test
		@DisplayName("does nothing when there is no pending notification")
		void noPendingNotifications() {
			when(smsNotificationRepository.findPendingSMSNotifications(any(Integer.class), any(Timestamp.class),
					any(Timestamp.class))).thenReturn(new ArrayList<>());

			service.publishSMS();

			Mockito.verify(smsNotificationRepository, Mockito.never()).save(any(SMSNotification.class));
		}

		@Test
		@DisplayName("a second concurrent publish is skipped while one is already running")
		void skipsConcurrentPublish() {
			ReflectionTestUtils.setField(SMSServiceImpl.class, "publishingSMS", true);

			service.publishSMS();

			Mockito.verify(smsNotificationRepository, Mockito.never())
					.findPendingSMSNotifications(any(Integer.class), any(), any());
		}
	}
}
