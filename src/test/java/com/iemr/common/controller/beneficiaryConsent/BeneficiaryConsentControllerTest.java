package com.iemr.common.controller.beneficiaryConsent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.data.beneficiaryConsent.BeneficiaryConsentRequest;
import com.iemr.common.exception.OtpRateLimitException;
import com.iemr.common.service.beneficiaryOTPHandler.BeneficiaryOTPHandler;

/**
 * Covers the public consent endpoints that issue and check a beneficiary's OTP.
 *
 * <p>Each endpoint distinguishes three outcomes: success, a rate-limit rejection reported
 * as 429 in the envelope, and any other failure reported as 500.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BeneficiaryConsentControllerTest {

	private static final String REQUEST = "{\"mobNo\":\"9876500000\",\"otp\":123456}";

	@Mock
	private BeneficiaryOTPHandler beneficiaryOTPHandler;

	@InjectMocks
	private BeneficiaryConsentController controller;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Nested
	@DisplayName("sendConsent")
	class SendConsent {

		@Test
		@DisplayName("reports the delivery outcome from the OTP handler")
		void reportsDeliveryOutcome() throws Exception {
			when(beneficiaryOTPHandler.sendOTP(any(BeneficiaryConsentRequest.class)))
					.thenReturn("OTP sent successfully on register mobile number");

			mockMvc.perform(post("/beneficiaryConsent/sendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(content().string(Matchers.containsString("OTP sent successfully")));
		}

		@Test
		@DisplayName("reports a rate limit rejection as 429")
		void reportsRateLimit() throws Exception {
			when(beneficiaryOTPHandler.sendOTP(any(BeneficiaryConsentRequest.class)))
					.thenThrow(new OtpRateLimitException("Maximum 3 OTPs allowed per minute"));

			mockMvc.perform(post("/beneficiaryConsent/sendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(429))
					.andExpect(jsonPath("$.errorMessage").value("Maximum 3 OTPs allowed per minute"));
		}

		@Test
		@DisplayName("reports any other failure as 500")
		void reportsOtherFailure() throws Exception {
			when(beneficiaryOTPHandler.sendOTP(any(BeneficiaryConsentRequest.class)))
					.thenThrow(new IllegalStateException("gateway down"));

			mockMvc.perform(post("/beneficiaryConsent/sendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(500));
		}
	}

	@Nested
	@DisplayName("validateConsent")
	class ValidateConsent {

		@Test
		@DisplayName("returns the session payload for a correct OTP")
		void returnsSessionPayload() throws Exception {
			JSONObject payload = new JSONObject();
			payload.put("key", "a-session-key");
			when(beneficiaryOTPHandler.validateOTP(any(BeneficiaryConsentRequest.class))).thenReturn(payload);

			mockMvc.perform(post("/beneficiaryConsent/validateConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.key").value("a-session-key"));
		}

		@Test
		@DisplayName("reports a failure when the handler returns nothing")
		void reportsMissingPayload() throws Exception {
			when(beneficiaryOTPHandler.validateOTP(any(BeneficiaryConsentRequest.class))).thenReturn(null);

			mockMvc.perform(post("/beneficiaryConsent/validateConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(500))
					.andExpect(jsonPath("$.errorMessage").value("failure"));
		}

		@Test
		@DisplayName("reports an incorrect OTP as a failure")
		void reportsIncorrectOtp() throws Exception {
			when(beneficiaryOTPHandler.validateOTP(any(BeneficiaryConsentRequest.class)))
					.thenThrow(new Exception("Please enter valid OTP"));

			mockMvc.perform(post("/beneficiaryConsent/validateConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(500))
					.andExpect(content().string(Matchers.containsString("Please enter valid OTP")));
		}
	}

	@Nested
	@DisplayName("resendConsent")
	class ResendConsent {

		@Test
		@DisplayName("reports success when the handler confirms an OTP was sent")
		void reportsSuccess() throws Exception {
			when(beneficiaryOTPHandler.resendOTP(any(BeneficiaryConsentRequest.class)))
					.thenReturn("otp sent successfully");

			mockMvc.perform(post("/beneficiaryConsent/resendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(content().string(Matchers.containsString("otp sent successfully")));
		}

		@Test
		@DisplayName("reports a failure when the handler's answer does not confirm an OTP")
		void reportsUnconfirmedDelivery() throws Exception {
			when(beneficiaryOTPHandler.resendOTP(any(BeneficiaryConsentRequest.class)))
					.thenReturn("Error sending SMS: gateway down");

			mockMvc.perform(post("/beneficiaryConsent/resendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(500))
					.andExpect(jsonPath("$.errorMessage").value("failure"));
		}

		@Test
		@DisplayName("reports a rate limit rejection as 429")
		void reportsRateLimit() throws Exception {
			when(beneficiaryOTPHandler.resendOTP(any(BeneficiaryConsentRequest.class)))
					.thenThrow(new OtpRateLimitException("Maximum 3 OTPs allowed per minute"));

			mockMvc.perform(post("/beneficiaryConsent/resendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(429));
		}

		@Test
		@DisplayName("reports any other failure as 500")
		void reportsOtherFailure() throws Exception {
			when(beneficiaryOTPHandler.resendOTP(any(BeneficiaryConsentRequest.class)))
					.thenThrow(new IllegalStateException("gateway down"));

			mockMvc.perform(post("/beneficiaryConsent/resendConsent").contentType(MediaType.APPLICATION_JSON)
					.content(REQUEST)).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(500));
		}
	}
}
