package org.acme.financials.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripFinancialsResponse(
        Long id,
        Long tripId,
        BigDecimal revenue,
        BigDecimal fuelCost,
        BigDecimal tollCost,
        BigDecimal driverAllowance,
        BigDecimal otherExpenses,
        BigDecimal netProfit,
        LocalDateTime createdAt
) {
}