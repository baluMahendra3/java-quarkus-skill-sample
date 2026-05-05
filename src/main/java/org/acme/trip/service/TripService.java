package org.acme.trip.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.acme.driver.entity.Driver;
import org.acme.driver.service.DriverService;
import org.acme.trip.dto.TripStatusUpdateResponse;
import org.acme.trip.entity.Trip;
import org.acme.trip.entity.TripStatus;
import org.acme.trip.repository.TripRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TripService {

    private final DriverService driverService;
    private final TripRepository tripRepository;

    public TripService(DriverService driverService, TripRepository tripRepository) {
        this.driverService = driverService;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public Trip createTrip(Trip trip, String createdBy) {
        if (trip.customerName == null || trip.fromLocation == null
                || trip.toLocation == null || trip.tripDate == null) {
            throw new ApiException(400, "customerName, fromLocation, toLocation, tripDate are required");
        }
        if (trip.driver != null && trip.driver.id != null) {
            Driver driver = resolveDriver(trip.driver.id);
            trip.driver = driver;
        }
        trip.createdBy = createdBy;
        tripRepository.persist(trip);
        return trip;
    }

    public List<Trip> listTrips(TripStatus status, Long driverId, String from, String to, Integer page, Integer size) {
        int pageIndex = resolvePage(page);
        int pageSize = resolveSize(size);
        if (driverId != null) {
            return tripRepository.findByDriver(driverId, pageIndex, pageSize);
        }
        if (status != null) {
            return tripRepository.findByStatus(status, pageIndex, pageSize);
        }
        if (from != null && to != null) {
            return tripRepository.findByDateRange(LocalDate.parse(from), LocalDate.parse(to), pageIndex, pageSize);
        }
        return tripRepository.listTrips(pageIndex, pageSize);
    }

    public Trip getTrip(Long id) {
        Trip trip = tripRepository.findById(id);
        if (trip == null) {
            throw new ApiException(404, "Trip not found");
        }
        return trip;
    }

    @Transactional
    public Trip updateTrip(Long id, Trip updated) {
        Trip trip = getTrip(id);
        if (updated.customerName != null) trip.customerName = updated.customerName;
        if (updated.customerPhone != null) trip.customerPhone = updated.customerPhone;
        if (updated.fromLocation != null) trip.fromLocation = updated.fromLocation;
        if (updated.toLocation != null) trip.toLocation = updated.toLocation;
        if (updated.tripDate != null) trip.tripDate = updated.tripDate;
        if (updated.tripType != null) trip.tripType = updated.tripType;
        if (updated.distanceKm != null) trip.distanceKm = updated.distanceKm;
        if (updated.notes != null) trip.notes = updated.notes;
        if (updated.driver != null && updated.driver.id != null) {
            trip.driver = resolveDriver(updated.driver.id);
        }
        return trip;
    }

    @Transactional
    public TripStatusUpdateResponse updateStatus(Long id, TripStatus status) {
        if (status == null) {
            throw new ApiException(400, "status query param required");
        }
        Trip trip = getTrip(id);
        trip.status = status;
        return new TripStatusUpdateResponse("Status updated", status);
    }

    @Transactional
    public MessageResponse cancelTrip(Long id) {
        Trip trip = getTrip(id);
        trip.status = TripStatus.CANCELLED;
        return new MessageResponse("Trip cancelled");
    }

    private Driver resolveDriver(Long driverId) {
        try {
            return driverService.getDriver(driverId);
        }
        catch (ApiException exception) {
            throw new ApiException(400, "Driver not found");
        }
    }

    private int resolvePage(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new ApiException(400, "page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolveSize(Integer size) {
        if (size == null) {
            return 50;
        }
        if (size < 1 || size > 100) {
            throw new ApiException(400, "size must be between 1 and 100");
        }
        return size;
    }
}