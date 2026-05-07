package org.acme.auth.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "RegisterResponse", description = "Response returned after a user is registered successfully")
public record RegisterResponse(
        @Schema(description = "Human-readable registration result", example = "User registered successfully")
        String message,
        @Schema(description = "Identifier of the newly created user", example = "42")
        Long userId
) {
}