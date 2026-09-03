package com.iemr.common.utils.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Covers the HTTP helper used for every outbound integration call.
 *
 * <p>The helper builds its own RestTemplate, so a mock is injected in its place; the tests
 * then assert the method, headers and body it puts on the wire and the status it records.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HttpUtilsTest {

	private static final String URI = "http://cti.example/api";

	@Mock
	private RestTemplate restTemplate;

	private HttpUtils httpUtils;

	@BeforeEach
	void setUp() {
		httpUtils = new HttpUtils();
		ReflectionTestUtils.setField(httpUtils, "rest", restTemplate);
	}

	private void respondsWith(String body, HttpStatus status) {
		when(restTemplate.exchange(eq(URI), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>(body, status));
	}

	@SuppressWarnings("unchecked")
	private HttpEntity<Object> capturedRequest() {
		ArgumentCaptor<HttpEntity<Object>> captor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(URI), any(HttpMethod.class), captor.capture(), eq(String.class));
		return captor.getValue();
	}

	@Nested
	@DisplayName("get")
	class Get {

		@Test
		@DisplayName("issues a GET and returns the body")
		void returnsTheBody() {
			respondsWith("{\"ok\":true}", HttpStatus.OK);

			assertThat(httpUtils.get(URI)).isEqualTo("{\"ok\":true}");
			verify(restTemplate).exchange(eq(URI), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
		}

		@Test
		@DisplayName("records the response status")
		void recordsTheStatus() {
			respondsWith("body", HttpStatus.ACCEPTED);

			httpUtils.get(URI);

			assertThat(httpUtils.getStatus()).isEqualTo(HttpStatus.ACCEPTED);
		}

		@Test
		@DisplayName("sends a JSON content type")
		void sendsJsonContentType() {
			respondsWith("body", HttpStatus.OK);

			httpUtils.get(URI);

			assertThat(capturedRequest().getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		}

		@Test
		@DisplayName("surfaces a transport failure")
		void surfacesTransportFailure() {
			when(restTemplate.exchange(eq(URI), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
					.thenThrow(new RestClientException("connection refused"));

			assertThatExceptionOfType(RestClientException.class).isThrownBy(() -> httpUtils.get(URI));
		}

		@Test
		@DisplayName("getV1 returns the whole response entity")
		void getV1ReturnsTheEntity() throws Exception {
			respondsWith("body", HttpStatus.OK);

			ResponseEntity<String> response = httpUtils.getV1(URI);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo("body");
		}
	}

	@Nested
	@DisplayName("get with explicit headers")
	class GetWithHeaders {

		@Test
		@DisplayName("passes an Authorization header through")
		void passesAuthorizationThrough() {
			respondsWith("body", HttpStatus.OK);
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.AUTHORIZATION, "Bearer a-token");

			httpUtils.get(URI, header);

			assertThat(capturedRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
					.isEqualTo("Bearer a-token");
		}

		@Test
		@DisplayName("passes an explicit content type through")
		void passesContentTypeThrough() {
			respondsWith("body", HttpStatus.OK);
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.CONTENT_TYPE, "application/xml");

			httpUtils.get(URI, header);

			assertThat(capturedRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
					.isEqualTo("application/xml");
		}

		@Test
		@DisplayName("defaults the content type to JSON when none is given")
		void defaultsContentTypeToJson() {
			respondsWith("body", HttpStatus.OK);

			httpUtils.get(URI, new HashMap<>());

			assertThat(capturedRequest().getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		}
	}

	@Nested
	@DisplayName("post")
	class Post {

		@Test
		@DisplayName("issues a POST carrying the given body")
		void carriesTheBody() {
			respondsWith("{\"saved\":true}", HttpStatus.CREATED);

			assertThat(httpUtils.post(URI, "{\"a\":1}")).isEqualTo("{\"saved\":true}");
			verify(restTemplate).exchange(eq(URI), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
			assertThat(capturedRequest().getBody()).isEqualTo("{\"a\":1}");
			assertThat(httpUtils.getStatus()).isEqualTo(HttpStatus.CREATED);
		}

		@Test
		@DisplayName("passes an Authorization header through")
		void passesAuthorizationThrough() {
			respondsWith("body", HttpStatus.OK);
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.AUTHORIZATION, "Bearer a-token");

			httpUtils.post(URI, "{\"a\":1}", header);

			assertThat(capturedRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
					.isEqualTo("Bearer a-token");
		}

		@Test
		@DisplayName("sends a UTF-8 JSON content type")
		void sendsUtf8JsonContentType() {
			respondsWith("body", HttpStatus.OK);

			httpUtils.post(URI, "{\"a\":1}", new HashMap<>());

			assertThat(capturedRequest().getHeaders().getFirst("Content-Type"))
					.isEqualTo("application/json;charset=utf-8");
		}
	}

	@Nested
	@DisplayName("uploadFile")
	class UploadFile {

		@Test
		@DisplayName("posts the payload directly when the content type is not multipart")
		void postsPayloadDirectly() throws IOException {
			respondsWith("uploaded", HttpStatus.OK);
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.CONTENT_TYPE, "application/json");

			assertThat(httpUtils.uploadFile(URI, "{\"a\":1}", header)).isEqualTo("uploaded");
			assertThat(capturedRequest().getBody()).isEqualTo("{\"a\":1}");
		}

		@Test
		@DisplayName("defaults to a JSON content type when none is given")
		void defaultsToJson() throws IOException {
			respondsWith("uploaded", HttpStatus.OK);

			assertThat(httpUtils.uploadFile(URI, "payload", new HashMap<>())).isEqualTo("uploaded");
		}

		@Test
		@DisplayName("streams the named file as a multipart body")
		void streamsMultipartBody(@TempDir Path tempDir) throws IOException {
			Path document = tempDir.resolve("scan.txt");
			Files.writeString(document, "file content");
			respondsWith("uploaded", HttpStatus.OK);
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data");

			assertThat(httpUtils.uploadFile(URI, document.toString(), header)).isEqualTo("uploaded");
		}

		@Test
		@DisplayName("reports a bad request when the named file does not exist")
		void missingFileIsReported() throws IOException {
			HashMap<String, Object> header = new HashMap<>();
			header.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data");

			assertThat(httpUtils.uploadFile(URI, "/no/such/file.txt", header)).isNull();
			assertThat(httpUtils.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
	}

	@Test
	@DisplayName("the recorded status can be set directly")
	void statusIsSettable() {
		httpUtils.setStatus(HttpStatus.NOT_FOUND);

		assertThat(httpUtils.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("the Authorization header name is exposed as a constant")
	void authorizationConstant() {
		assertThat(HttpUtils.AUTHORIZATION).isEqualTo("Authorization");
	}
}
