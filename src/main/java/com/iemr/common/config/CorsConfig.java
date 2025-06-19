package com.iemr.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // @PostConstruct
    // public void init() {
    // logger.info("✅ CorsConfig initialized with origins: {}", allowedOrigins);
    // System.out.println("✅ CorsConfig initialized with origins: " +
    // allowedOrigins);
    // }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] originArray = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        logger.info("🔧 Adding CORS mappings for origins: {}", Arrays.toString(originArray));
        System.out.println("🔧 Adding CORS mappings for origins: " + Arrays.toString(originArray));

        registry.addMapping("/**")
                .allowedOriginPatterns(originArray)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Jwttoken")
                .allowCredentials(true)
                .maxAge(3600);
    }
}