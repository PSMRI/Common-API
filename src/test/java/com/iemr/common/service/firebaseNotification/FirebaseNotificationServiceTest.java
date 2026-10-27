package com.iemr.common.service.firebaseNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.iemr.common.data.userToken.UserTokenData;
import com.iemr.common.model.notification.NotificationMessage;
import com.iemr.common.model.notification.UserToken;
import com.iemr.common.repo.userToken.UserTokenRepo;
import com.iemr.common.utils.CookieUtil;
import com.iemr.common.utils.JwtUtil;

/** Covers the Firebase push-notification service and its device-token bookkeeping. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebaseNotificationServiceTest {

	@Mock
	private UserTokenRepo userTokenRepo;
	@Mock
	private CookieUtil cookieUtil;
	@Mock
	private JwtUtil jwtUtil;

	private FirebaseNotificationService service;

	@BeforeEach
	void setUp() {
		service = new FirebaseNotificationService();
		ReflectionTestUtils.setField(service, "userTokenRepo", userTokenRepo);
		ReflectionTestUtils.setField(service, "cookieUtil", cookieUtil);
		ReflectionTestUtils.setField(service, "jwtUtil", jwtUtil);
	}

	@AfterEach
	void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private NotificationMessage notification() {
		NotificationMessage message = new NotificationMessage();
		message.setTitle("Call assigned");
		message.setBody("Beneficiary BEN-42 is waiting");
		message.setToken("topic-agents");
		message.setData(Map.of("callId", "42"));
		return message;
	}

	@Test
	@DisplayName("no configured Firebase messaging means the notification is skipped, not attempted")
	void notificationIsSkippedWhenFirebaseIsNotConfigured() {
		assertThat(service.sendNotification(notification())).isNull();
	}

	@Test
	@DisplayName("a configured Firebase publishes the notification to the requested topic")
	void notificationIsPublishedToTopic() throws Exception {
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);
		ReflectionTestUtils.setField(service, "firebaseMessaging", messaging);
		when(messaging.send(any(Message.class))).thenReturn("projects/amrit/messages/1");

		try (MockedStatic<FirebaseMessaging> statics = mockStatic(FirebaseMessaging.class)) {
			statics.when(FirebaseMessaging::getInstance).thenReturn(messaging);

			assertThat(service.sendNotification(notification())).isEqualTo("projects/amrit/messages/1");
			verify(messaging).send(any(Message.class));
		}
	}

	@Test
	@DisplayName("a Firebase delivery failure is reported rather than propagated")
	void deliveryFailureIsReported() throws Exception {
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);
		ReflectionTestUtils.setField(service, "firebaseMessaging", messaging);
		when(messaging.send(any(Message.class))).thenThrow(mock(FirebaseMessagingException.class));

		try (MockedStatic<FirebaseMessaging> statics = mockStatic(FirebaseMessaging.class)) {
			statics.when(FirebaseMessaging::getInstance).thenReturn(messaging);

			assertThat(service.sendNotification(notification())).isEqualTo("Error sending notification");
		}
	}

	@Test
	@DisplayName("an existing device token is updated in place")
	void existingTokenIsUpdated() {
		UserTokenData existing = new UserTokenData();
		existing.setUserId(7);
		existing.setToken("old-token");
		when(userTokenRepo.findById(7)).thenReturn(Optional.of(existing));

		UserToken userToken = new UserToken();
		userToken.setUserId(7);
		userToken.setToken("new-token");

		assertThat(service.updateToken(userToken)).isEqualTo("Save Successfully");

		ArgumentCaptor<UserTokenData> captor = ArgumentCaptor.forClass(UserTokenData.class);
		verify(userTokenRepo).save(captor.capture());
		assertThat(captor.getValue()).isSameAs(existing);
		assertThat(captor.getValue().getToken()).isEqualTo("new-token");
		assertThat(captor.getValue().getUpdatedAt()).isNotNull();
	}

	@Test
	@DisplayName("a user with no stored token gets a new row")
	void newTokenIsCreated() {
		when(userTokenRepo.findById(8)).thenReturn(Optional.empty());

		UserToken userToken = new UserToken();
		userToken.setUserId(8);
		userToken.setToken("fresh-token");

		assertThat(service.updateToken(userToken)).isEqualTo("Save Successfully");

		ArgumentCaptor<UserTokenData> captor = ArgumentCaptor.forClass(UserTokenData.class);
		verify(userTokenRepo).save(captor.capture());
		assertThat(captor.getValue().getUserId()).isEqualTo(8);
		assertThat(captor.getValue().getToken()).isEqualTo("fresh-token");
	}

	@Test
	@DisplayName("the caller's stored token is resolved from the JWT in their cookie")
	void storedTokenIsResolvedFromTheJwtCookie() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		when(jwtUtil.getUserIdFromToken("jwt-value")).thenReturn("11");
		UserTokenData stored = new UserTokenData();
		stored.setToken("device-token");
		when(userTokenRepo.findById(11)).thenReturn(Optional.of(stored));

		try (MockedStatic<CookieUtil> cookies = mockStatic(CookieUtil.class)) {
			cookies.when(() -> CookieUtil.getJwtTokenFromCookie(request)).thenReturn("jwt-value");

			assertThat(service.getUserToken()).isEqualTo("device-token");
		}
	}

	@Test
	@DisplayName("a caller with no stored token gets nothing back")
	void noStoredTokenYieldsNull() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		when(jwtUtil.getUserIdFromToken("jwt-value")).thenReturn("11");
		when(userTokenRepo.findById(11)).thenReturn(Optional.empty());

		try (MockedStatic<CookieUtil> cookies = mockStatic(CookieUtil.class)) {
			cookies.when(() -> CookieUtil.getJwtTokenFromCookie(request)).thenReturn("jwt-value");

			assertThat(service.getUserToken()).isNull();
		}
	}
}
