package com.iemr.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
    @Autowired
    private Environment env;

    @Bean
    public OpenAPI customOpenAPI() {
        String devUrl = env.getProperty("API_DEV_URL");
        String uatUrl = env.getProperty("API_UAT_URL");
        String demoUrl = env.getProperty("API_DEMO_URL");
        return new OpenAPI()
            .info(new Info().title("Common API").version("version").description("A microservice for the creation and management of beneficiaries."))
            .addSecurityItem(new SecurityRequirement().addList("my security"))
            .components(new Components().addSecuritySchemes("my security",
                new SecurityScheme().name("my security").type(SecurityScheme.Type.HTTP).scheme("bearer")))
            .servers(java.util.Arrays.asList(
                new io.swagger.v3.oas.models.servers.Server().url(devUrl).description("Dev"),
                new io.swagger.v3.oas.models.servers.Server().url(uatUrl).description("UAT"),
                new io.swagger.v3.oas.models.servers.Server().url(demoUrl).description("Demo")
            ));
    }

}
