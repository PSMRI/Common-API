package com.iemr.common.utils.gateway.email;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** Covers the plain-text email gateway used by the notification and feedback flows. */
@ExtendWith(MockitoExtension.class)
class GenericEmailServiceImplTest {

	private static final String PAYLOAD = "{\"to\":\"a@test;b@test\",\"from\":\"noreply@test\","
			+ "\"subject\":\"Subject\",\"message\":\"Body\"}";

	@Mock
	private JavaMailSender javaMailSender;

	private GenericEmailServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new GenericEmailServiceImpl();
		service.setJavaMailSender(javaMailSender);
	}

	@Test
	@DisplayName("a templated send addresses the recipient string as a single address")
	void templatedSendKeepsRecipientStringIntact() {
		service.sendEmail(PAYLOAD, "template");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(javaMailSender).send(captor.capture());
		SimpleMailMessage sent = captor.getValue();
		org.assertj.core.api.Assertions.assertThat(sent.getTo()).containsExactly("a@test;b@test");
		org.assertj.core.api.Assertions.assertThat(sent.getFrom()).isEqualTo("noreply@test");
		org.assertj.core.api.Assertions.assertThat(sent.getSubject()).isEqualTo("Subject");
		org.assertj.core.api.Assertions.assertThat(sent.getText()).isEqualTo("Body");
	}

	@Test
	@DisplayName("a plain send splits a semicolon-separated recipient list")
	void plainSendSplitsRecipients() {
		service.sendEmail(PAYLOAD);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(javaMailSender).send(captor.capture());
		org.assertj.core.api.Assertions.assertThat(captor.getValue().getTo()).containsExactly("a@test", "b@test");
	}

	@Test
	@DisplayName("a payload missing a required field is rejected before anything is sent")
	void missingFieldIsRejected() {
		assertThatThrownBy(() -> service.sendEmail("{\"to\":\"a@test\"}")).isInstanceOf(JSONException.class);

		verifyNoInteractions(javaMailSender);
	}

	@Test
	@DisplayName("attachments are not supported by this gateway and are a no-op")
	void attachmentSendIsANoOp() {
		assertThatCode(() -> service.sendEmailWithAttachment(PAYLOAD, "template")).doesNotThrowAnyException();

		verifyNoInteractions(javaMailSender);
	}
}
