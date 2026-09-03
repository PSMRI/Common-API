package com.iemr.common.service.beneficiaryOTPHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.cache.LoadingCache;
import com.iemr.common.data.beneficiaryConsent.BeneficiaryConsentRequest;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.sms.SMSTypeRepository;
import com.iemr.common.service.otp.OtpRateLimiterService;
import com.iemr.common.service.users.IEMRAdminUserServiceImpl;
import com.iemr.common.utils.http.HttpUtils;

/**
 * Covers OTP issue and validation for the beneficiary consent flow.
 *
 * <p>The OTP itself is held only as a SHA-256 digest in an in-memory cache, so the tests
 * assert on that contract: an issued OTP validates, anything else does not, and the rate
 * limiter is consulted before an OTP is ever generated. The SMS gateway is unreachable in
 * a unit test, so delivery failures are asserted as the reported error string.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BeneficiaryOTPHandlerImplTest {

	private static final String MOBILE = "9876500000";

	@Mock
	private HttpUtils httpUtils;
	@Mock
	private IEMRAdminUserServiceImpl iEMRAdminUserServiceImpl;
	@Mock
	private OtpRateLimiterService otpRateLimiterService;
	@Mock
	private SMSTemplateRepository smsTemplateRepository;
	@Mock
	private SMSTypeRepository smsTypeRepository;

	private BeneficiaryOTPHandlerImpl handler;

	@BeforeEach
	void setUp() {
		handler = new BeneficiaryOTPHandlerImpl();
		ReflectionTestUtils.setField(handler, "httpUtils", httpUtils);
		ReflectionTestUtils.setField(handler, "iEMRAdminUserServiceImpl", iEMRAdminUserServiceImpl);
		ReflectionTestUtils.setField(handler, "otpRateLimiterService", otpRateLimiterService);
		ReflectionTestUtils.setField(handler, "smsTemplateRepository", smsTemplateRepository);
		ReflectionTestUtils.setField(handler, "smsTypeRepository", smsTypeRepository);
		ReflectionTestUtils.setField(handler, "smsUserName", "sms-user");
		ReflectionTestUtils.setField(handler, "smsPassword", "sms-password");
		ReflectionTestUtils.setField(handler, "smsMessageType", "TRANS");
		ReflectionTestUtils.setField(handler, "smsEntityId", "entity-1");
		ReflectionTestUtils.setField(handler, "smsConsentSourceAddress", "AMRIT");
		// A closed local port makes the gateway call fail fast and without DNS.
		ReflectionTestUtils.setField(handler, "SMS_GATEWAY_URL", "http://127.0.0.1:1/sms");
	}

	private static BeneficiaryConsentRequest consentRequest() {
		BeneficiaryConsentRequest request = new BeneficiaryConsentRequest();
		request.setMobNo(MOBILE);
		return request;
	}

	private void withTemplate() {
		SMSTemplate template = new SMSTemplate();
		template.setSmsTemplateID(3);
		template.setSmsTemplateName("otp_consent");
		template.setSmsTemplate("Your OTP is $$OTP$$");
		template.setDltTemplateId("DLT-1");
		when(smsTemplateRepository.findBySmsTemplateName("otp_consent")).thenReturn(Optional.of(template));
		when(smsTemplateRepository.findBySmsTemplateID(3)).thenReturn(template);
		when(smsTemplateRepository.findAll()).thenReturn(List.of(template));
	}

	@SuppressWarnings("unchecked")
	private LoadingCache<String, String> otpCache() {
		return (LoadingCache<String, String>) ReflectionTestUtils.getField(handler, "otpCache");
	}

	@Nested
	@DisplayName("sendOTP")
	class SendOtp {

		@Test
		@DisplayName("checks the rate limit and caches a digest of the generated OTP")
		void issuesAndCachesAnOtp() throws Exception {
			withTemplate();

			handler.sendOTP(consentRequest());

			verify(otpRateLimiterService).checkRateLimit(MOBILE);
			assertThat(otpCache().get(MOBILE)).isNotEqualTo("0").hasSize(64);
		}

		@Test
		@DisplayName("reports the delivery failure when the SMS gateway cannot be reached")
		void reportsDeliveryFailure() throws Exception {
			withTemplate();

			assertThat(handler.sendOTP(consentRequest())).startsWith("Error sending SMS:");
		}

		@Test
		@DisplayName("does not generate an OTP when the caller is rate limited")
		void respectsTheRateLimiter() {
			doThrow(new RuntimeException("Too many OTP requests")).when(otpRateLimiterService).checkRateLimit(MOBILE);

			assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> handler.sendOTP(consentRequest()))
					.withMessageContaining("Too many OTP requests");
			assertThat(otpCache().asMap()).doesNotContainKey(MOBILE);
		}

		@Test
		@DisplayName("reports an error when no OTP template is configured")
		void reportsMissingTemplate() throws Exception {
			when(smsTemplateRepository.findBySmsTemplateName("otp_consent")).thenReturn(Optional.empty());
			when(smsTemplateRepository.findAll()).thenReturn(List.of());

			assertThat(handler.sendOTP(consentRequest())).startsWith("Error sending SMS:");
		}
	}

	@Nested
	@DisplayName("resendOTP")
	class ResendOtp {

		@Test
		@DisplayName("issues a fresh OTP, replacing the cached one")
		void issuesAFreshOtp() throws Exception {
			withTemplate();
			handler.sendOTP(consentRequest());
			String firstDigest = otpCache().get(MOBILE);

			handler.resendOTP(consentRequest());

			assertThat(otpCache().get(MOBILE)).isNotEqualTo("0");
			verify(otpRateLimiterService, org.mockito.Mockito.times(2)).checkRateLimit(MOBILE);
			assertThat(firstDigest).isNotBlank();
		}

		@Test
		@DisplayName("is rate limited like a first send")
		void respectsTheRateLimiter() {
			doThrow(new RuntimeException("Too many OTP requests")).when(otpRateLimiterService).checkRateLimit(MOBILE);

			assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> handler.resendOTP(consentRequest()));
		}
	}

	@Nested
	@DisplayName("validateOTP")
	class ValidateOtp {

		@Test
		@DisplayName("accepts the OTP that was issued and returns the generated session key")
		void acceptsTheIssuedOtp() throws Exception {
			int otp = handler.generateOTP(MOBILE);
			JSONObject expected = new JSONObject();
			expected.put("key", "a-session-key");
			when(iEMRAdminUserServiceImpl.generateKeyPostOTPValidation(org.mockito.ArgumentMatchers.any()))
					.thenReturn(expected);

			BeneficiaryConsentRequest request = consentRequest();
			request.setOtp(otp);

			assertThat(handler.validateOTP(request).getString("key")).isEqualTo("a-session-key");
		}

		@Test
		@DisplayName("passes the mobile number through as both the user name and id")
		void passesTheMobileNumberThrough() throws Exception {
			int otp = handler.generateOTP(MOBILE);
			when(iEMRAdminUserServiceImpl.generateKeyPostOTPValidation(org.mockito.ArgumentMatchers.any()))
					.thenAnswer(invocation -> invocation.getArgument(0));

			BeneficiaryConsentRequest request = consentRequest();
			request.setOtp(otp);

			JSONObject response = handler.validateOTP(request);
			assertThat(response.getString("userName")).isEqualTo(MOBILE);
			assertThat(response.getString("userID")).isEqualTo(MOBILE);
		}

		@Test
		@DisplayName("rejects an OTP that was never issued")
		void rejectsAnOtpThatWasNeverIssued() throws Exception {
			handler.generateOTP(MOBILE);

			BeneficiaryConsentRequest request = consentRequest();
			request.setOtp(111111);

			assertThatExceptionOfType(Exception.class).isThrownBy(() -> handler.validateOTP(request))
					.withMessageContaining("Please enter valid OTP");
			verify(iEMRAdminUserServiceImpl, never())
					.generateKeyPostOTPValidation(org.mockito.ArgumentMatchers.any());
		}

		@Test
		@DisplayName("rejects validation for a number that was never sent an OTP")
		void rejectsUnknownMobileNumber() {
			BeneficiaryConsentRequest request = consentRequest();
			request.setOtp(123456);

			assertThatExceptionOfType(Exception.class).isThrownBy(() -> handler.validateOTP(request))
					.withMessageContaining("Please enter valid OTP");
		}
	}

	@Nested
	@DisplayName("generateOTP")
	class GenerateOtp {

		@Test
		@DisplayName("produces a six digit code")
		void producesSixDigits() throws Exception {
			assertThat(handler.generateOTP(MOBILE)).isBetween(100000, 999999);
		}

		@Test
		@DisplayName("caches only a digest, never the code itself")
		void cachesOnlyADigest() throws Exception {
			int otp = handler.generateOTP(MOBILE);

			assertThat(otpCache().get(MOBILE)).doesNotContain(String.valueOf(otp)).hasSize(64);
		}

		@Test
		@DisplayName("issues independent codes per mobile number")
		void issuesPerNumber() throws Exception {
			handler.generateOTP(MOBILE);
			handler.generateOTP("9000000000");

			assertThat(otpCache().get(MOBILE)).isNotEqualTo(otpCache().get("9000000000"));
		}

		@Test
		@DisplayName("a fresh code replaces the previous one for the same number")
		void replacesThePreviousCode() throws Exception {
			handler.generateOTP(MOBILE);
			String first = otpCache().get(MOBILE);
			handler.generateOTP(MOBILE);

			assertThat(otpCache().get(MOBILE)).isNotEqualTo("0");
			assertThat(first).hasSize(64);
		}
	}

	@Test
	@DisplayName("an unseen mobile number has no cached OTP")
	void unseenNumberHasNoCachedOtp() throws Exception {
		assertThat(otpCache().get("9111111111")).isEqualTo("0");
	}
}
