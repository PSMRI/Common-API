package com.iemr.common.utils;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FilterConfig {

	private static final Logger logger = LoggerFactory.getLogger(FilterConfig.class);

	@Value("${cors.allowed-origins}")
	private String allowedOrigins;

	@Bean
	public FilterRegistrationBean<JwtUserIdValidationFilter> jwtUserIdValidationFilter(
			JwtAuthenticationUtil jwtAuthenticationUtil) {
		logger.info("Registering JwtUserIdValidationFilter with allowed origins: {}", allowedOrigins);

		FilterRegistrationBean<JwtUserIdValidationFilter> registrationBean = new FilterRegistrationBean<>();

		// Pass allowedOrigins explicitly to the filter constructor
		JwtUserIdValidationFilter filter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, allowedOrigins);

		registrationBean.setFilter(filter);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registrationBean.addUrlPatterns("/*"); // Apply filter to all API endpoints

		logger.info("JwtUserIdValidationFilter registered successfully with order {}", Ordered.HIGHEST_PRECEDENCE);

		return registrationBean;
	}
}
