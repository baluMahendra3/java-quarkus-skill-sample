package org.acme.auth.dto;

import org.acme.common.Role;

import java.time.LocalDateTime;

public class UserResponse {
    public Long id;
    public String name;
    public String email;
    public Role role;
    public boolean active;
    public LocalDateTime createdAt;
}