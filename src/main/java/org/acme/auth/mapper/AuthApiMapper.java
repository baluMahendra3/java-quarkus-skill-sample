package org.acme.auth.mapper;

import org.acme.auth.dto.UserResponse;
import org.acme.auth.entity.User;

public final class AuthApiMapper {

    private AuthApiMapper() {
    }

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.id = user.id;
        response.name = user.name;
        response.email = user.email;
        response.role = user.role;
        response.active = user.active;
        response.createdAt = user.createdAt;
        return response;
    }
}