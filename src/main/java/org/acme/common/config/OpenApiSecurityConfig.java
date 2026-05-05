package org.acme.common.config;

import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT bearer token for protected travel management endpoints"
)
public final class OpenApiSecurityConfig {

    private OpenApiSecurityConfig() {
    }
}