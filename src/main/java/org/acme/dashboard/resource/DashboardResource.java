package org.acme.dashboard.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.common.error.ErrorResponse;
import org.acme.dashboard.dto.DashboardSummary;
import org.acme.dashboard.mapper.DashboardApiMapper;
import org.acme.dashboard.service.DashboardService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Business summary and KPI metrics")
@APIResponses({
    @APIResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "403", description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @APIResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class DashboardResource {

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GET
        @Operation(
            summary = "Get full business dashboard summary",
            description = "Returns the top-level KPI summary for trips, drivers, revenue, and profit.",
            operationId = "getDashboardSummary"
        )
        @APIResponse(
            responseCode = "200",
            description = "Dashboard summary returned successfully",
            content = @Content(schema = @Schema(implementation = DashboardSummary.class))
        )
    public DashboardSummary getDashboard() {
        return DashboardApiMapper.toResponse(dashboardService.getDashboard());
    }

    @GET
    @Path("/reports/summary")
        @Operation(
            summary = "Get financial summary report",
            description = "Returns the same dashboard metrics through the reporting-specific summary endpoint.",
            operationId = "getFinancialSummary"
        )
        @APIResponse(
            responseCode = "200",
            description = "Financial summary returned successfully",
            content = @Content(schema = @Schema(implementation = DashboardSummary.class))
        )
    public DashboardSummary getFinancialSummary() {
        return getDashboard();
    }
}