package org.acme.financials.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TripFinancialsRequest(
        @PositiveOrZero
        BigDecimal revenue,
        @PositiveOrZero
        BigDecimal fuelCost,
        @PositiveOrZero
        BigDecimal tollCost,
        @PositiveOrZero
        BigDecimal driverAllowance,
        @PositiveOrZero
        BigDecimal otherExpenses
) {
}