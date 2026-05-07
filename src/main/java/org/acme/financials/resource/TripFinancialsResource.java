package org.acme.financials.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.common.error.ErrorResponse;
import org.acme.financials.dto.TripFinancialsRequest;
import org.acme.financials.dto.TripFinancialsResponse;
import org.acme.financials.entity.TripFinancials;
import org.acme.financials.mapper.TripFinancialsApiMapper;
import org.acme.financials.service.TripFinancialsService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/trips/{tripId}/financials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Financials", description = "Trip profit and loss operations")
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
public class TripFinancialsResource {

    private static final Logger LOG = LoggerFactory.getLogger(TripFinancialsResource.class);

    private final TripFinancialsService tripFinancialsService;

    public TripFinancialsResource(TripFinancialsService tripFinancialsService) {
        this.tripFinancialsService = tripFinancialsService;
    }

    @POST
        @Operation(
            summary = "Add or update financials for a trip",
            description = "Creates or replaces the financial snapshot for a trip and recalculates net profit.",
            operationId = "upsertTripFinancials"
        )
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TripFinancialsRequest.class)))
        @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Financials created successfully",
            content = @Content(schema = @Schema(implementation = TripFinancialsResponse.class))
        ),
        @APIResponse(
            responseCode = "200",
            description = "Financials updated successfully",
            content = @Content(schema = @Schema(implementation = TripFinancialsResponse.class))
        )
        })
    public Response upsertFinancials(@PathParam("tripId") Long tripId, @Valid TripFinancialsRequest request) {
        boolean existing = tripFinancialsService.findFinancials(tripId).isPresent();
        LOG.info("event=trip.financials.upsert.request tripId={} action={}", tripId, existing ? "update" : "create");
        TripFinancials financials = tripFinancialsService.upsertFinancials(
                tripId,
                TripFinancialsApiMapper.toEntity(request)
        );
        return existing
                ? Response.ok(TripFinancialsApiMapper.toResponse(financials)).build()
                : Response.status(Response.Status.CREATED).entity(TripFinancialsApiMapper.toResponse(financials)).build();
    }

    @GET
    @Operation(
            summary = "Get financials for a trip",
            description = "Returns the current financial metrics for a specific trip.",
            operationId = "getTripFinancials"
    )
    @APIResponse(
            responseCode = "200",
            description = "Financials returned successfully",
            content = @Content(schema = @Schema(implementation = TripFinancialsResponse.class))
    )
    public Response getFinancials(@PathParam("tripId") Long tripId) {
        LOG.info("event=trip.financials.get.request tripId={}", tripId);
        return Response.ok(TripFinancialsApiMapper.toResponse(tripFinancialsService.getFinancials(tripId))).build();
    }
}