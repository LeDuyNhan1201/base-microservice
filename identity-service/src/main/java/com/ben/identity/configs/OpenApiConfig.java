package com.ben.identity.configs;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.ben.common.configs.OpenApiConfig.createOpenAPI;

@Configuration
@Profile("!prod")
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi(@Value("${openapi.service.api-docs}") String apiDocs) {
        return GroupedOpenApi.builder()
            .group(apiDocs)
            .packagesToScan("com.ben.identity.controllers")
            .build();
    }

    @Bean
    public OpenAPI openAPI(
        @Value("${openapi.service.title}") String title,
        @Value("${openapi.service.description}") String description,
        @Value("${openapi.service.version}") String version,
        @Value("${openapi.service.server-url}") String serverUrl,
        @Value("${openapi.service.server-description}") String serverDescription,
        @Value("${gateway.domain}") String gatewayDomain,
        @Value("${gateway.port}") String gatewayPort,
        @Value("${server.servlet.context-path}") String contextPath
    ) {
        return createOpenAPI(
            title,
            description,
            version,
            serverUrl,
            serverDescription,
            gatewayDomain,
            gatewayPort,
            contextPath
        );
    }

}
