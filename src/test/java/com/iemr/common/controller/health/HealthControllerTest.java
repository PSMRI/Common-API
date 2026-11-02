package com.iemr.common.controller.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.iemr.common.service.health.HealthService;

/** Covers the /health endpoint's mapping of a health report onto an HTTP status. */
@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

	@Mock
	private HealthService healthService;

	@InjectMocks
	private HealthController controller;

	@Test
	@DisplayName("an UP report is served as 200 with the full report body")
	void upReportIsServedAsOk() {
		Map<String, Object> report = new LinkedHashMap<>();
		report.put("status", "UP");
		report.put("mysql", Map.of("status", "UP"));
		when(healthService.checkHealth()).thenReturn(report);

		ResponseEntity<Map<String, Object>> response = controller.health();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isSameAs(report);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "DOWN", "DEGRADED", "up" })
	@DisplayName("anything other than UP is served as 503 so load balancers take the node out")
	void anythingButUpIsUnavailable(String status) {
		Map<String, Object> report = new LinkedHashMap<>();
		report.put("status", status);
		when(healthService.checkHealth()).thenReturn(report);

		assertThat(controller.health().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}
}
