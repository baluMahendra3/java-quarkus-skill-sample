package org.acme.driver.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ErrorResponse;
import org.acme.driver.dto.DriverRequest;
import org.acme.driver.dto.DriverResponse;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;
import org.acme.driver.mapper.DriverApiMapper;
import org.acme.driver.service.DriverService;
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

@Path("/api/drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Drivers", description = "Driver management operations")
@APIResponses({
    @APIResponse(responseCode = "400", description = "Bad request",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "403", description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "404", description = "Resource not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class DriverResource {

    private final DriverService driverService;

    public DriverResource(DriverService driverService) {
        this.driverService = driverService;
    }

    @POST
        @Operation(
            summary = "Create a new driver",
            description = "Registers a driver and initializes active status and audit fields.",
            operationId = "createDriver"
        )
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DriverRequest.class)))
        @APIResponse(
            responseCode = "201",
            description = "Driver created successfully",
            content = @Content(schema = @Schema(implementation = DriverResponse.class))
        )
    public Response createDriver(@Valid DriverRequest request) {
        Driver driver = driverService.createDriver(DriverApiMapper.toEntity(request));
        return Response.status(Response.Status.CREATED).entity(DriverApiMapper.toResponse(driver)).build();
    }

    @GET
        @Operation(
            summary = "List all drivers",
            description = "Returns a paged list of drivers with optional status filtering.",
            operationId = "listDrivers"
        )
        @APIResponse(
            responseCode = "200",
            description = "Drivers returned successfully",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = DriverResponse.class))
        )
    public List<DriverResponse> listDrivers(
            @Parameter(description = "Optional driver status filter", schema = @Schema(implementation = DriverStatus.class))
            @QueryParam("status") DriverStatus status,
            @Parameter(description = "Zero-based page index", schema = @Schema(defaultValue = "0", minimum = "0"))
            @QueryParam("page") @Min(0) Integer page,
            @Parameter(description = "Maximum number of drivers per page", schema = @Schema(defaultValue = "50", minimum = "1", maximum = "100"))
            @QueryParam("size") @Min(1) @Max(100) Integer size) {
        return driverService.listDrivers(status, page, size).stream()
                .map(DriverApiMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
        @Operation(
            summary = "Get driver details by ID",
            description = "Returns the full driver profile for a known driver identifier.",
            operationId = "getDriver"
        )
        @APIResponse(
            responseCode = "200",
            description = "Driver returned successfully",
            content = @Content(schema = @Schema(implementation = DriverResponse.class))
        )
    public Response getDriver(@PathParam("id") Long id) {
        return Response.ok(DriverApiMapper.toResponse(driverService.getDriver(id))).build();
    }

    @PUT
    @Path("/{id}")
        @Operation(
            summary = "Update driver information",
            description = "Updates mutable driver fields such as contact details, vehicle, and status.",
            operationId = "updateDriver"
        )
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DriverRequest.class)))
        @APIResponse(
            responseCode = "200",
            description = "Driver updated successfully",
            content = @Content(schema = @Schema(implementation = DriverResponse.class))
        )
    public Response updateDriver(@PathParam("id") Long id, @Valid DriverRequest request) {
        Driver driver = driverService.updateDriver(id, DriverApiMapper.toEntity(request));
        return Response.ok(DriverApiMapper.toResponse(driver)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Deactivate a driver (Admin only)",
            description = "Marks an existing driver as inactive.",
            operationId = "deactivateDriver"
    )
    @APIResponse(
            responseCode = "200",
            description = "Driver deactivated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
    )
    public Response deactivateDriver(@PathParam("id") Long id) {
        return Response.ok(driverService.deactivateDriver(id)).build();
    }
}