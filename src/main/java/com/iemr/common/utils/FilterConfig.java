/*
 * AMRIT – Accessible Medical Records via Integrated Technology
 * Integrated EHR (Electronic Health Records) Solution
 *
 * Copyright (C) "Piramal Swasthya Management and Research Institute"
 *
 * This file is part of AMRIT.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.common.utils;

import com.iemr.common.filter.PlatformFeedbackRateLimitFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class FilterConfig {

	@Value("${cors.allowed-origins}")
	private String allowedOrigins;

    @Bean
    public FilterRegistrationBean<JwtUserIdValidationFilter> jwtUserIdValidationFilter(
            JwtAuthenticationUtil jwtAuthenticationUtil) {
        FilterRegistrationBean<JwtUserIdValidationFilter> registrationBean = new FilterRegistrationBean<>();

        // Pass allowedOrigins explicitly to the filter constructor
        JwtUserIdValidationFilter filter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, allowedOrigins);

        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*"); // Apply filter to all API endpoints
        log.info("Registered JwtUserIdValidationFilter on /* with order {}", Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    /**
     * Register the platform feedback rate-limit filter in a non-invasive way.
     *
     * - The filter is mapped only to the public feedback endpoints to avoid affecting other routes.
     * - Order is intentionally set after the Jwt filter so authentication runs first.
     */
    @Bean
    public FilterRegistrationBean<PlatformFeedbackRateLimitFilter> platformFeedbackRateLimitFilter(
            StringRedisTemplate stringRedisTemplate,
            Environment env) {

        // Read flag from environment (property file or env var)
        boolean enabled = Boolean.parseBoolean(env.getProperty("platform.feedback.ratelimit.enabled", "false"));

        // Allow optional override for order if needed
        int defaultOrder = Ordered.HIGHEST_PRECEDENCE + 10;
        int order = defaultOrder;
        String orderStr = env.getProperty("platform.feedback.ratelimit.order");
        if (orderStr != null) {
            try {
                order = Integer.parseInt(orderStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid platform.feedback.ratelimit.order value '{}', using default {}", orderStr, defaultOrder);
            }
        }

        PlatformFeedbackRateLimitFilter filter = new PlatformFeedbackRateLimitFilter(stringRedisTemplate, env);

        FilterRegistrationBean<PlatformFeedbackRateLimitFilter> reg = new FilterRegistrationBean<>(filter);

        reg.addUrlPatterns(
                "/platform-feedback/*",
                "/platform-feedback"
        );

        reg.setOrder(order);
        // Do not remove the bean from context when disabled; keep registration but disable execution
        reg.setEnabled(enabled);

        log.info("Registered PlatformFeedbackRateLimitFilter (enabled={}, order={}) mapped to {}",
                enabled, order, reg.getUrlPatterns());

        return reg;
    }
}