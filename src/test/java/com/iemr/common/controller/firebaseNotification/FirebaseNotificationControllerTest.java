package com.iemr.common.controller.firebaseNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.common.model.notification.NotificationMessage;
import com.iemr.common.model.notification.UserToken;
import com.iemr.common.service.firebaseNotification.FirebaseNotificationService;
import com.iemr.common.utils.exception.IEMRException;

/** Covers the Firebase push-notification endpoints. */
@ExtendWith(MockitoExtension.class)
class FirebaseNotificationControllerTest {

	@Mock
	private FirebaseNotificationService firebaseNotificationService;

	@InjectMocks
	private FirebaseNotificationController controller;

	@Test
	@DisplayName("sending a notification returns the message id the service reports")
	void sendNotificationReturnsMessageId() {
		NotificationMessage message = new NotificationMessage();
		when(firebaseNotificationService.sendNotification(message)).thenReturn("projects/amrit/messages/1");

		assertThat(controller.sendNotificationByToken(message)).isEqualTo("projects/amrit/messages/1");
	}

	@Test
	@DisplayName("updating a device token returns the service's confirmation")
	void updateTokenReturnsConfirmation() {
		UserToken userToken = new UserToken();
		when(firebaseNotificationService.updateToken(userToken)).thenReturn("Save Successfully");

		assertThat(controller.updateToken(userToken)).isEqualTo("Save Successfully");
	}

	@Test
	@DisplayName("fetching the caller's token returns what the service resolved")
	void getTokenReturnsStoredToken() throws Exception {
		when(firebaseNotificationService.getUserToken()).thenReturn("device-token");

		assertThat(controller.getUserToken()).isEqualTo("device-token");
	}

	@Test
	@DisplayName("a failure resolving the caller's token is surfaced to the caller")
	void getTokenPropagatesFailure() throws Exception {
		when(firebaseNotificationService.getUserToken()).thenThrow(new IEMRException("no session"));

		assertThatThrownBy(() -> controller.getUserToken()).isInstanceOf(IEMRException.class);
	}
}
