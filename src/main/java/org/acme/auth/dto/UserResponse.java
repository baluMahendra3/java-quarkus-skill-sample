package org.acme.auth.dto;

import org.acme.common.Role;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "UserResponse", description = "Administrative view of a registered user account")
public class UserResponse {
    @Schema(description = "Unique user identifier", example = "42")
    public Long id;

    @Schema(description = "Display name of the user", example = "Operations Manager")
    public String name;

    @Schema(description = "User email address", example = "manager@travel.local")
    public String email;

    @Schema(description = "Assigned application role", example = "MANAGER")
    public Role role;

    @Schema(description = "Whether the account is active", example = "true")
    public boolean active;

    @Schema(description = "Timestamp when the account was created", example = "2026-05-06T00:15:30")
    public LocalDateTime createdAt;
}