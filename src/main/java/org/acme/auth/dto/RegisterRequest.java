package org.acme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.acme.common.Role;

public class RegisterRequest {
    @NotBlank
    public String name;

    @Email
    @NotBlank
    public String email;

    @NotBlank
    public String password;

    public Role role;
}