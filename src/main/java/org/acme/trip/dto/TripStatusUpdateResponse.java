package org.acme.trip.dto;

import org.acme.trip.entity.TripStatus;

public record TripStatusUpdateResponse(
        String message,
        TripStatus status
) {
}