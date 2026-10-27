package com.iemr.common.service.welcomeSms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.sms.SMSTypeRepository;

/** Covers the welcome SMS sent to a newly registered beneficiary. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WelcomeBenificarySmsServiceImplTest {

	@Mock
	private SMSTemplateRepository smsTemplateRepository;
	@Mock
	private SMSTypeRepository smsTypeRepository;

	private WelcomeBenificarySmsServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new WelcomeBenificarySmsServiceImpl();
		ReflectionTestUtils.setField(service, "smsTemplateRepository", smsTemplateRepository);
		ReflectionTestUtils.setField(service, "smsTypeRepository", smsTypeRepository);
		ReflectionTestUtils.setField(service, "smsUserName", "sms-user");
		ReflectionTestUtils.setField(service, "smsPassword", "sms-secret");
		ReflectionTestUtils.setField(service, "smsEntityId", "entity-1");
		ReflectionTestUtils.setField(service, "smsSourceAddress", "AMRIT");
		ReflectionTestUtils.setField(service, "SMS_GATEWAY_URL", "http://sms.test/send");
	}

	private void stubTemplate(String body) {
		SMSTemplate template = new SMSTemplate();
		ReflectionTestUtils.setField(template, "smsTemplateID", 5);
		ReflectionTestUtils.setField(template, "dltTemplateId", "dlt-9");
		ReflectionTestUtils.setField(template, "smsTemplate", body);
		when(smsTemplateRepository.findBySmsTemplateName("welcome_sms")).thenReturn(Optional.of(template));
		when(smsTemplateRepository.findBySmsTemplateID(5)).thenReturn(template);
	}

	@Test
	@DisplayName("the template placeholders are filled and the gateway called with basic auth")
	void placeholdersAreFilledAndGatewayCalled() {
		stubTemplate("Welcome $$BENE_NAME$$, your id is $$BEN_ID$$");

		try (MockedConstruction<RestTemplate> construction = mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.postForEntity(anyString(), any(), eq(String.class)))
						.thenReturn(new ResponseEntity<>("queued", HttpStatus.OK)))) {

			String result = service.sendWelcomeSMStoBenificiary("9999999999", "Latha", "BEN-42");

			assertThat(result).isEqualTo("OTP sent successfully on register mobile number");

			RestTemplate restTemplate = construction.constructed().get(0);
			@SuppressWarnings("unchecked")
			org.mockito.ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = org.mockito.ArgumentCaptor
					.forClass(HttpEntity.class);
			org.mockito.Mockito.verify(restTemplate).postForEntity(eq("http://sms.test/send"), captor.capture(),
					eq(String.class));
			HttpEntity<Map<String, Object>> request = captor.getValue();
			assertThat(request.getBody()).containsEntry("message", "Welcome Latha, your id is BEN-42")
					.containsEntry("destinationAddress", "9999999999").containsEntry("customerId", "sms-user")
					.containsEntry("sourceAddress", "AMRIT").containsEntry("messageType", "SERVICE_IMPLICIT")
					.containsEntry("dltTemplateId", "dlt-9").containsEntry("entityId", "entity-1");
			assertThat(request.getHeaders().getFirst("Authorization"))
					.isEqualTo("Basic " + Base64.getEncoder().encodeToString("sms-user:sms-secret".getBytes()));
		}
	}

	@Test
	@DisplayName("a non-200 gateway reply is reported as a failure")
	void nonOkGatewayReplyIsAFailure() {
		stubTemplate("Welcome $$BENE_NAME$$");

		try (MockedConstruction<RestTemplate> construction = mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.postForEntity(anyString(), any(), eq(String.class)))
						.thenReturn(new ResponseEntity<>("rejected", HttpStatus.ACCEPTED)))) {

			assertThat(service.sendWelcomeSMStoBenificiary("9999999999", "Latha", "BEN-42")).isEqualTo("Fail");
		}
	}

	@Test
	@DisplayName("a gateway error is reported with its message rather than thrown")
	void gatewayErrorIsReported() {
		stubTemplate("Welcome $$BENE_NAME$$");

		try (MockedConstruction<RestTemplate> construction = mockConstruction(RestTemplate.class,
				(mock, context) -> when(mock.postForEntity(anyString(), any(), eq(String.class)))
						.thenThrow(new RestClientException("gateway unreachable")))) {

			assertThat(service.sendWelcomeSMStoBenificiary("9999999999", "Latha", "BEN-42"))
					.isEqualTo("Error sending SMS: gateway unreachable");
		}
	}

	@Test
	@DisplayName("no configured template means no SMS is attempted")
	void missingTemplateSendsNothing() {
		when(smsTemplateRepository.findBySmsTemplateName("welcome_sms")).thenReturn(Optional.empty());

		try (MockedConstruction<RestTemplate> construction = mockConstruction(RestTemplate.class)) {
			assertThat(service.sendWelcomeSMStoBenificiary("9999999999", "Latha", "BEN-42")).isNull();
			org.mockito.Mockito.verifyNoInteractions(construction.constructed().get(0));
		}
	}
}
