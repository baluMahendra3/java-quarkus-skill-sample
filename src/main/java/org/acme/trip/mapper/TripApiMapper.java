package org.acme.trip.mapper;

import org.acme.driver.entity.Driver;
import org.acme.trip.dto.TripDriverResponse;
import org.acme.trip.dto.TripRequest;
import org.acme.trip.dto.TripResponse;
import org.acme.trip.entity.Trip;

public final class TripApiMapper {

    private TripApiMapper() {
    }

    public static Trip toEntity(TripRequest request) {
        Trip trip = new Trip();
        trip.customerName = request.customerName();
        trip.customerPhone = request.customerPhone();
        trip.fromLocation = request.fromLocation();
        trip.toLocation = request.toLocation();
        trip.tripDate = request.tripDate();
        trip.tripType = request.tripType();
        trip.distanceKm = request.distanceKm();
        trip.notes = request.notes();
        if (request.driverId() != null) {
            Driver driver = new Driver();
            driver.id = request.driverId();
            trip.driver = driver;
        }
        return trip;
    }

    public static TripResponse toResponse(Trip trip) {
        TripDriverResponse driver = trip.driver == null
                ? null
                : new TripDriverResponse(trip.driver.id, trip.driver.name, trip.driver.phone, trip.driver.status);

        return new TripResponse(
                trip.id,
                trip.tripCode,
                driver,
                trip.customerName,
                trip.customerPhone,
                trip.fromLocation,
                trip.toLocation,
                trip.tripDate,
                trip.tripType,
                trip.distanceKm,
                trip.status,
                trip.notes,
                trip.createdBy,
                trip.createdAt
        );
    }
}