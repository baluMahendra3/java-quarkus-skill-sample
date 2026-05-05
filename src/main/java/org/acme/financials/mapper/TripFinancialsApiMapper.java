package org.acme.financials.mapper;

import org.acme.financials.dto.TripFinancialsRequest;
import org.acme.financials.dto.TripFinancialsResponse;
import org.acme.financials.entity.TripFinancials;

public final class TripFinancialsApiMapper {

    private TripFinancialsApiMapper() {
    }

    public static TripFinancials toEntity(TripFinancialsRequest request) {
        TripFinancials financials = new TripFinancials();
        financials.revenue = request.revenue();
        financials.fuelCost = request.fuelCost();
        financials.tollCost = request.tollCost();
        financials.driverAllowance = request.driverAllowance();
        financials.otherExpenses = request.otherExpenses();
        return financials;
    }

    public static TripFinancialsResponse toResponse(TripFinancials financials) {
        return new TripFinancialsResponse(
                financials.id,
                financials.trip != null ? financials.trip.id : null,
                financials.revenue,
                financials.fuelCost,
                financials.tollCost,
                financials.driverAllowance,
                financials.otherExpenses,
                financials.netProfit,
                financials.createdAt
        );
    }
}