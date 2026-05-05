package org.acme.trip.dto;

import org.acme.trip.entity.TripStatus;
import org.acme.trip.entity.TripType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        String tripCode,
        TripDriverResponse driver,
        String customerName,
        String customerPhone,
        String fromLocation,
        String toLocation,
        LocalDate tripDate,
        TripType tripType,
        Double distanceKm,
        TripStatus status,
        String notes,
        String createdBy,
        LocalDateTime createdAt
) {
}