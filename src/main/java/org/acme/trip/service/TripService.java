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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@ApplicationScoped
public class TripService {

    private static final Logger LOG = LoggerFactory.getLogger(TripService.class);

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
            LOG.warn("event=trip.create.invalidRequest missingRequiredFields=true");
            throw new ApiException(400, "customerName, fromLocation, toLocation, tripDate are required");
        }
        if (trip.driver != null && trip.driver.id != null) {
            Driver driver = resolveDriver(trip.driver.id);
            trip.driver = driver;
        }
        trip.createdBy = createdBy;
        tripRepository.persist(trip);
        LOG.info("event=trip.create.completed tripId={} createdBy={} hasDriver={}", trip.id, createdBy, trip.driver != null);
        return trip;
    }

    public List<Trip> listTrips(TripStatus status, Long driverId, String from, String to, Integer page, Integer size) {
        int pageIndex = resolvePage(page);
        int pageSize = resolveSize(size);
        List<Trip> trips;
        if (driverId != null) {
            trips = tripRepository.findByDriver(driverId, pageIndex, pageSize);
        }
        else if (status != null) {
            trips = tripRepository.findByStatus(status, pageIndex, pageSize);
        }
        else if (from != null && to != null) {
            trips = tripRepository.findByDateRange(
                    parseDateFilter(from, "from"),
                    parseDateFilter(to, "to"),
                    pageIndex,
                    pageSize
            );
        }
        else {
            trips = tripRepository.listTrips(pageIndex, pageSize);
        }
        LOG.info(
                "event=trip.list.completed status={} driverId={} hasDateRange={} page={} size={} resultCount={}",
                status,
                driverId,
                from != null && to != null,
                pageIndex,
                pageSize,
                trips.size()
        );
        return trips;
    }

    public Trip getTrip(Long id) {
        Trip trip = tripRepository.findById(id);
        if (trip == null) {
            LOG.warn("event=trip.get.notFound tripId={}", id);
            throw new ApiException(404, "Trip not found");
        }
        LOG.info("event=trip.get.completed tripId={} status={}", trip.id, trip.status);
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
        LOG.info("event=trip.update.completed tripId={} hasDriver={} status={}", trip.id, trip.driver != null, trip.status);
        return trip;
    }

    @Transactional
    public TripStatusUpdateResponse updateStatus(Long id, TripStatus status) {
        if (status == null) {
            LOG.warn("event=trip.status.update.invalidRequest missingStatus=true tripId={}", id);
            throw new ApiException(400, "status query param required");
        }
        Trip trip = getTrip(id);
        trip.status = status;
        LOG.info("event=trip.status.update.completed tripId={} status={}", trip.id, status);
        return new TripStatusUpdateResponse("Status updated", status);
    }

    @Transactional
    public MessageResponse cancelTrip(Long id) {
        Trip trip = getTrip(id);
        trip.status = TripStatus.CANCELLED;
        LOG.info("event=trip.cancel.completed tripId={}", trip.id);
        return new MessageResponse("Trip cancelled");
    }

    private Driver resolveDriver(Long driverId) {
        try {
            return driverService.getDriver(driverId);
        }
        catch (ApiException exception) {
            LOG.warn("event=trip.driver.notFound driverId={}", driverId);
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

    private LocalDate parseDateFilter(String value, String parameterName) {
        try {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException exception) {
            throw new ApiException(400, parameterName + " must be a valid ISO-8601 date (yyyy-MM-dd)");
        }
    }
}