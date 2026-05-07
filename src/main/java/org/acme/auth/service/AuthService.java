package org.acme.auth.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.auth.JwtService;
import org.acme.auth.dto.LoginResponse;
import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RegisterResponse;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.entity.User;
import org.acme.auth.repository.AuthRepository;
import org.acme.common.Role;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final JwtService jwtService;
    private final AuthRepository authRepository;

    public AuthService(JwtService jwtService, AuthRepository authRepository) {
        this.jwtService = jwtService;
        this.authRepository = authRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (request.email == null || request.password == null || request.name == null) {
            LOG.warn("event=auth.register.invalidRequest missingRequiredFields=true");
            throw new ApiException(400, "name, email and password are required");
        }
        if (authRepository.findByEmail(request.email) != null) {
            LOG.warn("event=auth.register.duplicateEmail");
            throw new ApiException(409, "Email already registered");
        }

        User user = new User();
        user.name = request.name;
        user.email = request.email;
        user.password = BcryptUtil.bcryptHash(request.password);
        user.role = request.role != null ? request.role : Role.MANAGER;
        authRepository.persist(user);
        LOG.info("event=auth.register.completed userId={} role={}", user.id, user.role);

        return new RegisterResponse("User registered", user.id);
    }

    public LoginResponse login(LoginRequest request) {
        if (request.email == null || request.password == null) {
            LOG.warn("event=auth.login.invalidRequest missingCredentials=true");
            throw new ApiException(400, "email and password are required");
        }

        User user = authRepository.findByEmail(request.email);
        if (user == null || !BcryptUtil.matches(request.password, user.password)) {
            LOG.warn("event=auth.login.invalidCredentials");
            throw new ApiException(401, "Invalid credentials");
        }
        if (!user.active) {
            LOG.warn("event=auth.login.inactiveAccount userId={}", user.id);
            throw new ApiException(403, "Account is inactive");
        }

        String token = jwtService.generateToken(user.id, user.email, user.role);
        LOG.info("event=auth.login.completed userId={} role={}", user.id, user.role);
        return new LoginResponse(
            token,
            user.id,
            user.name,
            user.email,
            user.role.name()
        );
    }

    public List<User> listUsers(Integer page, Integer size) {
        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);
        List<User> users = authRepository.listUsers(resolvedPage, resolvedSize);
        LOG.info("event=auth.users.list.completed page={} size={} resultCount={}", resolvedPage, resolvedSize, users.size());
        return users;
    }

    @Transactional
    public MessageResponse deactivateUser(Long id) {
        User user = authRepository.findById(id);
        if (user == null) {
            LOG.warn("event=auth.user.deactivate.notFound userId={}", id);
            throw new ApiException(404, "User not found");
        }

        user.active = false;
        LOG.info("event=auth.user.deactivate.completed userId={}", id);
        return new MessageResponse("User deactivated");
    }

    private int resolvePage(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new ApiException(400, "page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolveSize(Integer size) {
        if (size == null) {
            return 50;
        }
        if (size < 1 || size > 100) {
            throw new ApiException(400, "size must be between 1 and 100");
        }
        return size;
    }
}