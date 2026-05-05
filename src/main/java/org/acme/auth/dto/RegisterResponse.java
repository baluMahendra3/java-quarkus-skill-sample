package org.acme.auth.dto;

public record RegisterResponse(
        String message,
        Long userId
) {
}