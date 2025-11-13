package com.iemr.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Spring MVC CORS configuration (framework level).
     * 
     * NOTE: This configuration is permissive at the Spring framework level.
     * Actual granular CORS enforcement (origin validation, endpoint-specific method control)
     * is handled by JwtUserIdValidationFilter, which implements a two-layer security approach:
     * 
     * 1. Spring CORS config: Permissive at framework level (allows PUT/DELETE for all endpoints)
     * 2. JwtUserIdValidationFilter: Enforces strict origin validation and endpoint-specific method restrictions
     * 
     * This design allows Spring to handle CORS preflight requests, while the filter enforces
     * security policies before requests reach controllers.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type", "Accept", "Jwttoken",
            "serverAuthorization", "ServerAuthorization", "serverauthorization", "Serverauthorization")
                .exposedHeaders("Authorization", "Jwttoken")
                .allowCredentials(true)
                .maxAge(3600);
    }
}