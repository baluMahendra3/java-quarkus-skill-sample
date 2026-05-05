package org.acme.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.common.Role;

import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    private static final long TOKEN_EXPIRY_SECONDS = 86400L;

    public String generateToken(Long userId, String email, Role role) {
        return Jwt.issuer("travel-app")
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .groups(Set.of(role.name()))
                .expiresAt(Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS))
                .sign();
    }
}
