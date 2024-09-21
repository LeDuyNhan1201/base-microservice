package com.ben.profile.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("!prod")
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi(@Value("${openapi.service.api-docs}") String apiDocs) {
        return GroupedOpenApi.builder()
            .group(apiDocs)
            .packagesToScan("com.ben.profile.controllers")
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
        @Value("${gateway.port}") String gatewayPort
    ) {
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl).description(serverDescription)))
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact()
                                .email("benlun99999@gmail.com")
                                .name("Ben's side projects")
                        )
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Find out more about this service")
                        .url("http://abc.com")
                )
                .servers(List.of(new Server().url(String.format("http://%s:%s/profile", gatewayDomain, gatewayPort))))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .security(List.of(
                        new SecurityRequirement()
                                .addList("bearerAuth")));
    }

}
