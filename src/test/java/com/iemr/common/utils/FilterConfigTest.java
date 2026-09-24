package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.test.util.ReflectionTestUtils;

/** Covers the servlet-filter registration that guards every endpoint with the JWT user check. */
class FilterConfigTest {

	@Test
	@DisplayName("the JWT filter runs first and is mapped to every request")
	void jwtFilterIsRegisteredFirstForEveryRequest() {
		FilterConfig config = new FilterConfig();
		ReflectionTestUtils.setField(config, "allowedOrigins", "http://a.test,http://b.test");
		JwtAuthenticationUtil jwtAuthenticationUtil = mock(JwtAuthenticationUtil.class);

		FilterRegistrationBean<JwtUserIdValidationFilter> registration = config
				.jwtUserIdValidationFilter(jwtAuthenticationUtil);

		assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
		assertThat(registration.getUrlPatterns()).containsExactly("/*");
		assertThat(registration.getFilter()).isInstanceOf(JwtUserIdValidationFilter.class);
	}
}
