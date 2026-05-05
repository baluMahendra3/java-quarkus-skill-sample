package org.acme.auth.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.LoginResponse;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.dto.RegisterResponse;
import org.acme.auth.dto.UserResponse;
import org.acme.auth.mapper.AuthApiMapper;
import org.acme.auth.service.AuthService;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ErrorResponse;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Register, login and user management")
@APIResponses({
    @APIResponse(responseCode = "400", description = "Bad request",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "403", description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "404", description = "Resource not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "409", description = "Conflict",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/register")
    @PermitAll
        @Operation(
            summary = "Register a new user",
            description = "Creates a manager or role-specific user account and stores a hashed password.",
            operationId = "registerUser"
        )
        @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class))
        )
        @APIResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = RegisterResponse.class))
        )
    public Response register(@Valid RegisterRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(authService.register(request)).build();
    }

    @POST
    @Path("/login")
    @PermitAll
        @Operation(
            summary = "Login and receive JWT token",
            description = "Authenticates a user and returns a signed JWT along with basic account information.",
            operationId = "loginUser"
        )
        @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class))
        )
        @APIResponse(
            responseCode = "200",
            description = "Authenticated successfully",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        )
    public Response login(@Valid LoginRequest request) {
        return Response.ok(authService.login(request)).build();
    }

    @GET
    @Path("/users")
    @RolesAllowed("ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(
            summary = "List users (Admin only)",
            description = "Returns a paged list of registered users visible to administrators.",
            operationId = "listUsers"
        )
        @APIResponse(
            responseCode = "200",
            description = "Users returned successfully",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = UserResponse.class))
        )
    public List<UserResponse> listUsers(
            @Parameter(description = "Zero-based page index", schema = @Schema(defaultValue = "0", minimum = "0"))
            @QueryParam("page") @Min(0) Integer page,
            @Parameter(description = "Maximum number of users per page", schema = @Schema(defaultValue = "50", minimum = "1", maximum = "100"))
            @QueryParam("size") @Min(1) @Max(100) Integer size) {
        return authService.listUsers(page, size).stream()
                .map(AuthApiMapper::toResponse)
                .toList();
    }

    @PUT
    @Path("/users/{id}/deactivate")
    @RolesAllowed("ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Deactivate a user (Admin only)",
            description = "Marks a user account as inactive without deleting it.",
            operationId = "deactivateUser"
    )
    @APIResponse(
            responseCode = "200",
            description = "User deactivated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
    )
    public Response deactivateUser(@PathParam("id") Long id) {
        return Response.ok(authService.deactivateUser(id)).build();
    }
}