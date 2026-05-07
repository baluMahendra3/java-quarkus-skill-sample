package org.acme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Credentials used to authenticate an existing user")
public class LoginRequest {
    @Email
    @NotBlank
    @Schema(description = "Registered email address used as the username", example = "manager@travel.local")
    public String email;

    @NotBlank
    @Schema(description = "Plain-text password submitted for authentication", example = "P@ssword123")
    public String password;
}