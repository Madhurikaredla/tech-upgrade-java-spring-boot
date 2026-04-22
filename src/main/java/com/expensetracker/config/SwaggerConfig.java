package com.expensetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig — configures the Springdoc OpenAPI 3.0 specification.
 *
 * Rules from CLAUDE.md §15:
 *  - Swagger UI path  : /swagger-ui.html
 *  - API groups       : Auth, Expenses, Categories
 *  - Accept-Language header documented on all endpoints (via controller annotations)
 *  - Swagger UI disabled in production (springdoc.api-docs.enabled=false in prod yml)
 *
 * A global JWT Bearer security scheme is registered so all non-public
 * endpoints display the "Authorize" button in the Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    /** Security scheme name referenced by SecurityRequirement on protected endpoints. */
    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    /**
     * Configures the OpenAPI specification with API metadata and a global
     * JWT Bearer security scheme.
     *
     * @return fully configured OpenAPI bean
     */
    @Bean
    public OpenAPI expenseTrackerOpenApi() {
        return new OpenAPI()
                .info(apiInfo())
                // Declare a global security requirement so all protected endpoints
                // show the padlock icon in Swagger UI.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_AUTH_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter the JWT token obtained from /api/v1/auth/login")));
    }

    /**
     * API metadata displayed in the Swagger UI header.
     *
     * @return populated Info object
     */
    private Info apiInfo() {
        return new Info()
                .title("Expense Tracker API")
                .description("""
                        RESTful API for the Expense Tracker System.
                        
                        Supports user registration, authentication, expense management,
                        and category management. All protected endpoints require a
                        valid JWT Bearer token.
                        
                        Internationalization: pass an Accept-Language header (en | hi | te)
                        to receive error messages in the preferred language.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Expense Tracker Team"));
    }
}
