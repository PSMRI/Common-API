package com.iemr.common.controller.carestream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers the CareStream HL7 order endpoint against a loopback listener.
 *
 * <p>The controller writes an HL7 ORM message straight onto a socket, so a throwaway
 * server socket stands in for the imaging system: it captures what the controller sent and
 * replies with an acknowledgement. That lets the message content be asserted rather than
 * only the fact that a call was attempted.
 */
class CareStreamCreateOrderControllerSocketTest {

	private static final String ORDER_REQUEST = "{\"firstName\":\"Latha\",\"LastName\":\"Devi\","
			+ "\"patientID\":\"BEN-4242\",\"dob\":\"19900101\",\"gender\":\"F\",\"acc\":\"ACC-1\"}";

	private CareStreamCreateOrderController controller;
	private ServerSocket serverSocket;
	private Thread listener;
	private final AtomicReference<String> received = new AtomicReference<>();
	private final CountDownLatch exchanged = new CountDownLatch(1);

	@BeforeEach
	void setUp() {
		controller = new CareStreamCreateOrderController();
		// OutputResponse serialises through OutputMapper's static Gson builders.
		new com.iemr.common.utils.mapper.OutputMapper();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		if (listener != null) {
			listener.join(TimeUnit.SECONDS.toMillis(5));
		}
	}

	/** Starts a listener that records the HL7 message and answers with {@code reply}. */
	private void startListener(String reply) throws IOException {
		serverSocket = new ServerSocket(0);
		ReflectionTestUtils.setField(controller, "carestreamSocketIP", "127.0.0.1");
		ReflectionTestUtils.setField(controller, "carestreamSocketPort", serverSocket.getLocalPort());

		listener = new Thread(() -> {
			try (Socket client = serverSocket.accept()) {
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();
				byte[] buffer = new byte[4096];
				int read = in.read(buffer);
				if (read > 0) {
					received.set(new String(buffer, 0, read, StandardCharsets.UTF_8));
				}
				if (reply != null) {
					out.write(reply.getBytes(StandardCharsets.UTF_8));
					out.flush();
				}
			} catch (IOException ignored) {
				// the socket was closed while the test was tearing down
			} finally {
				exchanged.countDown();
			}
		}, "carestream-test-listener");
		listener.setDaemon(true);
		listener.start();
	}

	private void awaitExchange() throws InterruptedException {
		assertThat(exchanged.await(10, TimeUnit.SECONDS)).as("the listener should have completed").isTrue();
	}

	@Nested
	@DisplayName("createOrder")
	class CreateOrder {

		@Test
		@DisplayName("reports success once the imaging system acknowledges the order")
		void reportsSuccess() throws Exception {
			startListener("MSH|ACK|");

			String response = controller.createOrder(ORDER_REQUEST);

			awaitExchange();
			assertThat(response).contains("Order successfully created");
		}

		@Test
		@DisplayName("sends an HL7 ORM message carrying the patient and order details")
		void sendsAnHl7Message() throws Exception {
			startListener("MSH|ACK|");

			controller.createOrder(ORDER_REQUEST);

			awaitExchange();
			String sent = received.get();
			assertThat(sent).isNotNull();
			assertThat(sent).contains("MSH|").contains("ORM^O01");
			assertThat(sent).contains("PID|||BEN-4242||Latha^Devi^||19900101|F");
			assertThat(sent).contains("ORC|NW||ACC-1");
			assertThat(sent).contains("CR_CHEST_PA");
		}

		@Test
		@DisplayName("reports an error when the imaging system cannot be reached")
		void reportsUnreachableSystem() throws Exception {
			ReflectionTestUtils.setField(controller, "carestreamSocketIP", "127.0.0.1");
			// A closed local port refuses immediately and without DNS.
			ReflectionTestUtils.setField(controller, "carestreamSocketPort", 1);

			assertThat(controller.createOrder(ORDER_REQUEST)).contains("statusCode");
		}

		@Test
		@DisplayName("reports an error for a request it cannot parse")
		void reportsUnparseableRequest() throws Exception {
			ReflectionTestUtils.setField(controller, "carestreamSocketIP", "127.0.0.1");
			ReflectionTestUtils.setField(controller, "carestreamSocketPort", 1);

			assertThat(controller.createOrder("{not json")).contains("statusCode");
		}

		@Test
		@DisplayName("leaves the response empty when the imaging system answers with nothing")
		void silentImagingSystem() throws Exception {
			startListener(null);

			String response = controller.createOrder(ORDER_REQUEST);

			awaitExchange();
			// Nothing was read back, so no outcome was recorded on the response.
			assertThat(response).doesNotContain("Order successfully created");
		}

		@Test
		@DisplayName("carries through an order with no patient details")
		void handlesAnEmptyOrder() throws Exception {
			startListener("MSH|ACK|");

			String response = controller.createOrder("{}");

			awaitExchange();
			assertThat(response).contains("Order successfully created");
			assertThat(received.get()).contains("PID|||null||null^null^||null|null");
		}
	}
}
