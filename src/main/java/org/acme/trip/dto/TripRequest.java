package org.acme.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.acme.trip.entity.TripType;

import java.time.LocalDate;

public record TripRequest(
        @NotBlank
        String customerName,
        String customerPhone,
        @NotBlank
        String fromLocation,
        @NotBlank
        String toLocation,
        @NotNull
        LocalDate tripDate,
        TripType tripType,
        @PositiveOrZero
        Double distanceKm,
        String notes,
        Long driverId
) {
}