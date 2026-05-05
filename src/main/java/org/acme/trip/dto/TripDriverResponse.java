package org.acme.trip.dto;

import org.acme.driver.entity.DriverStatus;

public record TripDriverResponse(
        Long id,
        String name,
        String phone,
        DriverStatus status
) {
}