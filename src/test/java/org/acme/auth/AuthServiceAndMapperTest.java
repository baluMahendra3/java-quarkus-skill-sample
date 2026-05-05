package org.acme.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import org.acme.auth.dto.LoginResponse;
import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RegisterResponse;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.dto.UserResponse;
import org.acme.auth.entity.User;
import org.acme.auth.mapper.AuthApiMapper;
import org.acme.auth.repository.AuthRepository;
import org.acme.auth.service.AuthService;
import org.acme.common.Role;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceAndMapperTest {

    @Test
    void registerShouldPersistUserAndDefaultRole() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        when(authRepository.findByEmail("alice@example.com")).thenReturn(null);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.id = 10L;
            return null;
        }).when(authRepository).persist(any(User.class));

        AuthService service = new AuthService(jwtService, authRepository);
        RegisterRequest request = new RegisterRequest();
        request.name = "Alice";
        request.email = "alice@example.com";
        request.password = "secret";

        RegisterResponse response = service.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(authRepository).persist(captor.capture());
        User saved = captor.getValue();
        assertEquals("Alice", saved.name);
        assertEquals("alice@example.com", saved.email);
        assertTrue(BcryptUtil.matches("secret", saved.password));
        assertEquals(Role.MANAGER, saved.role);
        assertEquals("User registered", response.message());
        assertEquals(10L, response.userId());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        when(authRepository.findByEmail("alice@example.com")).thenReturn(new User());

        AuthService service = new AuthService(jwtService, authRepository);
        RegisterRequest request = new RegisterRequest();
        request.name = "Alice";
        request.email = "alice@example.com";
        request.password = "secret";

        ApiException exception = assertThrows(ApiException.class, () -> service.register(request));
        assertEquals(409, exception.getStatus());
        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void loginShouldReturnTokenForActiveUser() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        User user = new User();
        user.id = 99L;
        user.name = "Alice";
        user.email = "alice@example.com";
        user.password = BcryptUtil.bcryptHash("secret");
        user.role = Role.ADMIN;
        user.active = true;
        when(authRepository.findByEmail("alice@example.com")).thenReturn(user);
        when(jwtService.generateToken(99L, "alice@example.com", Role.ADMIN)).thenReturn("jwt-token");

        AuthService service = new AuthService(jwtService, authRepository);
        LoginRequest request = new LoginRequest();
        request.email = "alice@example.com";
        request.password = "secret";

        LoginResponse response = service.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals(99L, response.userId());
        assertEquals("ADMIN", response.role());
    }

    @Test
    void loginShouldRejectMissingFieldsAndInactiveUser() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        AuthService service = new AuthService(jwtService, authRepository);

        LoginRequest missing = new LoginRequest();
        ApiException missingException = assertThrows(ApiException.class, () -> service.login(missing));
        assertEquals(400, missingException.getStatus());

        User inactive = new User();
        inactive.email = "alice@example.com";
        inactive.password = BcryptUtil.bcryptHash("secret");
        inactive.role = Role.MANAGER;
        inactive.active = false;
        when(authRepository.findByEmail("alice@example.com")).thenReturn(inactive);

        LoginRequest request = new LoginRequest();
        request.email = "alice@example.com";
        request.password = "secret";

        ApiException inactiveException = assertThrows(ApiException.class, () -> service.login(request));
        assertEquals(403, inactiveException.getStatus());
        assertEquals("Account is inactive", inactiveException.getMessage());
    }

    @Test
    void registerShouldRejectMissingRequiredFields() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        AuthService service = new AuthService(jwtService, authRepository);

        ApiException exception = assertThrows(ApiException.class, () -> service.register(new RegisterRequest()));
        assertEquals(400, exception.getStatus());
        assertEquals("name, email and password are required", exception.getMessage());
    }

    @Test
    void listUsersShouldResolveDefaultPagingAndValidation() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        when(authRepository.listUsers(0, 50)).thenReturn(List.of(new User()));

        AuthService service = new AuthService(jwtService, authRepository);

        assertEquals(1, service.listUsers(null, null).size());
        verify(authRepository).listUsers(0, 50);

        ApiException exception = assertThrows(ApiException.class, () -> service.listUsers(0, 101));
        assertEquals(400, exception.getStatus());
        assertEquals("size must be between 1 and 100", exception.getMessage());
    }

    @Test
    void deactivateUserShouldFlipActiveFlag() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        User user = new User();
        user.active = true;
        when(authRepository.findById(10L)).thenReturn(user);

        AuthService service = new AuthService(jwtService, authRepository);

        MessageResponse response = service.deactivateUser(10L);

        assertFalse(user.active);
        assertEquals("User deactivated", response.message());
    }

    @Test
    void deactivateUserShouldFailWhenMissing() {
        JwtService jwtService = mock(JwtService.class);
        AuthRepository authRepository = mock(AuthRepository.class);
        when(authRepository.findById(404L)).thenReturn(null);
        AuthService service = new AuthService(jwtService, authRepository);

        ApiException exception = assertThrows(ApiException.class, () -> service.deactivateUser(404L));
        assertEquals(404, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void authApiMapperShouldCopyFields() {
        User user = new User();
        user.id = 7L;
        user.name = "Alice";
        user.email = "alice@example.com";
        user.role = Role.MANAGER;
        user.active = true;
        user.createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        UserResponse response = AuthApiMapper.toResponse(user);

        assertEquals(7L, response.id);
        assertEquals("Alice", response.name);
        assertEquals("alice@example.com", response.email);
        assertEquals(Role.MANAGER, response.role);
        assertTrue(response.active);
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), response.createdAt);
    }
}