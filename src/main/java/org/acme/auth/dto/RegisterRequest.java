package org.acme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.acme.common.Role;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "RegisterRequest", description = "Payload used to create a new application user")
public class RegisterRequest {
    @NotBlank
    @Schema(description = "Full display name of the user being created", example = "Operations Manager")
    public String name;

    @Email
    @NotBlank
    @Schema(description = "Unique email address for the new user", example = "manager@travel.local")
    public String email;

    @NotBlank
    @Schema(description = "Initial password for the user account", example = "P@ssword123")
    public String password;

    @Schema(description = "Role granted to the new user", example = "MANAGER")
    public Role role;
}