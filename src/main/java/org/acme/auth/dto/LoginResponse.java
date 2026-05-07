package org.acme.auth.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Authentication result containing the signed access token and account summary")
public record LoginResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxIn0.signature")
        String token,
        @Schema(description = "Authenticated user identifier", example = "42")
        Long userId,
        @Schema(description = "Authenticated user display name", example = "Operations Manager")
        String name,
        @Schema(description = "Authenticated user email address", example = "manager@travel.local")
        String email,
        @Schema(description = "Role assigned to the authenticated user", example = "MANAGER")
        String role
) {
}