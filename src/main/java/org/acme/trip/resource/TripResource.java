package org.acme.trip.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ErrorResponse;
import org.acme.trip.dto.TripRequest;
import org.acme.trip.dto.TripResponse;
import org.acme.trip.dto.TripStatusUpdateResponse;
import org.acme.trip.entity.Trip;
import org.acme.trip.entity.TripStatus;
import org.acme.trip.mapper.TripApiMapper;
import org.acme.trip.service.TripService;
import org.eclipse.microprofile.jwt.JsonWebToken;
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

@Path("/api/trips")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Trips", description = "Trip management operations")
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
public class TripResource {

    private final TripService tripService;
    private final JsonWebToken jwt;

    public TripResource(TripService tripService, JsonWebToken jwt) {
        this.tripService = tripService;
        this.jwt = jwt;
    }

    @POST
        @Operation(
            summary = "Create a new trip",
            description = "Creates a scheduled trip and captures the authenticated creator identity.",
            operationId = "createTrip"
        )
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TripRequest.class)))
        @APIResponse(
            responseCode = "201",
            description = "Trip created successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))
        )
    public Response createTrip(@Valid TripRequest request) {
        Trip trip = TripApiMapper.toEntity(request);
        return Response.status(Response.Status.CREATED)
                .entity(TripApiMapper.toResponse(tripService.createTrip(trip, jwt.getSubject()))).build();
    }

    @GET
        @Operation(
            summary = "List all trips with optional filters",
            description = "Returns a paged trip list filtered by status, driver, or trip date range.",
            operationId = "listTrips"
        )
        @APIResponse(
            responseCode = "200",
            description = "Trips returned successfully",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = TripResponse.class))
        )
    public List<TripResponse> listTrips(
            @Parameter(description = "Optional trip status filter", schema = @Schema(implementation = TripStatus.class))
            @QueryParam("status") TripStatus status,
            @Parameter(description = "Optional driver identifier filter")
            @QueryParam("driverId") Long driverId,
            @Parameter(description = "Inclusive trip-date start filter in ISO-8601 format")
            @QueryParam("from") String from,
            @Parameter(description = "Inclusive trip-date end filter in ISO-8601 format")
            @QueryParam("to") String to,
            @Parameter(description = "Zero-based page index", schema = @Schema(defaultValue = "0", minimum = "0"))
            @QueryParam("page") @Min(0) Integer page,
            @Parameter(description = "Maximum number of trips per page", schema = @Schema(defaultValue = "50", minimum = "1", maximum = "100"))
            @QueryParam("size") @Min(1) @Max(100) Integer size) {
        return tripService.listTrips(status, driverId, from, to, page, size).stream()
                .map(TripApiMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
        @Operation(
            summary = "Get trip details by ID",
            description = "Returns the full trip details for a known trip identifier.",
            operationId = "getTrip"
        )
        @APIResponse(
            responseCode = "200",
            description = "Trip returned successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))
        )
    public Response getTrip(@PathParam("id") Long id) {
        return Response.ok(TripApiMapper.toResponse(tripService.getTrip(id))).build();
    }

    @PUT
    @Path("/{id}")
        @Operation(
            summary = "Update trip details",
            description = "Updates mutable trip details such as customer, route, schedule, and notes.",
            operationId = "updateTrip"
        )
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TripRequest.class)))
        @APIResponse(
            responseCode = "200",
            description = "Trip updated successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))
        )
    public Response updateTrip(@PathParam("id") Long id, @Valid TripRequest request) {
        Trip trip = tripService.updateTrip(id, TripApiMapper.toEntity(request));
        return Response.ok(TripApiMapper.toResponse(trip)).build();
    }

    @PUT
    @Path("/{id}/status")
        @Operation(
            summary = "Update trip status",
            description = "Transitions the trip status to a new lifecycle state.",
            operationId = "updateTripStatus"
        )
        @APIResponse(
            responseCode = "200",
            description = "Trip status updated successfully",
            content = @Content(schema = @Schema(implementation = TripStatusUpdateResponse.class))
        )
    public Response updateStatus(@PathParam("id") Long id, @QueryParam("status") TripStatus status) {
        return Response.ok(tripService.updateStatus(id, status)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Cancel a trip (Admin only)",
            description = "Cancels a trip without deleting it from the system.",
            operationId = "cancelTrip"
    )
    @APIResponse(
            responseCode = "200",
            description = "Trip cancelled successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
    )
    public Response cancelTrip(@PathParam("id") Long id) {
        return Response.ok(tripService.cancelTrip(id)).build();
    }
}