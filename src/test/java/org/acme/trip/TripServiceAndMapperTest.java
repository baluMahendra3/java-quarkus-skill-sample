package org.acme.trip;

import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;
import org.acme.driver.service.DriverService;
import org.acme.trip.dto.TripDriverResponse;
import org.acme.trip.dto.TripRequest;
import org.acme.trip.dto.TripResponse;
import org.acme.trip.dto.TripStatusUpdateResponse;
import org.acme.trip.entity.Trip;
import org.acme.trip.entity.TripStatus;
import org.acme.trip.entity.TripType;
import org.acme.trip.mapper.TripApiMapper;
import org.acme.trip.repository.TripRepository;
import org.acme.trip.service.TripService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceAndMapperTest {

    @Test
    void createTripShouldResolveDriverPersistAndSetCreatedBy() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        Driver driver = new Driver();
        driver.id = 4L;
        when(driverService.getDriver(4L)).thenReturn(driver);
        doAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.id = 30L;
            return null;
        }).when(tripRepository).persist(any(Trip.class));
        TripService service = new TripService(driverService, tripRepository);

        Trip trip = new Trip();
        trip.customerName = "Customer";
        trip.fromLocation = "A";
        trip.toLocation = "B";
        trip.tripDate = LocalDate.of(2026, 4, 10);
        trip.driver = new Driver();
        trip.driver.id = 4L;

        Trip created = service.createTrip(trip, "system");

        verify(tripRepository).persist(trip);
        assertEquals(30L, created.id);
        assertEquals("system", created.createdBy);
        assertEquals(4L, created.driver.id);
    }

    @Test
    void createTripShouldRejectMissingFieldsAndUnknownDriver() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        when(driverService.getDriver(55L)).thenThrow(new ApiException(404, "Driver not found"));
        TripService service = new TripService(driverService, tripRepository);

        ApiException missingFields = assertThrows(ApiException.class, () -> service.createTrip(new Trip(), "system"));
        assertEquals(400, missingFields.getStatus());

        Trip trip = new Trip();
        trip.customerName = "Customer";
        trip.fromLocation = "A";
        trip.toLocation = "B";
        trip.tripDate = LocalDate.of(2026, 4, 10);
        trip.driver = new Driver();
        trip.driver.id = 55L;

        ApiException driverMissing = assertThrows(ApiException.class, () -> service.createTrip(trip, "system"));
        assertEquals(400, driverMissing.getStatus());
        assertEquals("Driver not found", driverMissing.getMessage());
    }

    @Test
    void listTripsShouldExerciseAllFilterBranches() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        when(tripRepository.findByDriver(7L, 0, 50)).thenReturn(List.of(new Trip()));
        when(tripRepository.findByStatus(TripStatus.COMPLETED, 0, 50)).thenReturn(List.of(new Trip()));
        when(tripRepository.findByDateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 0, 50)).thenReturn(List.of(new Trip()));
        when(tripRepository.listTrips(0, 50)).thenReturn(List.of(new Trip()));
        TripService service = new TripService(driverService, tripRepository);

        assertEquals(1, service.listTrips(null, 7L, null, null, null, null).size());
        assertEquals(1, service.listTrips(TripStatus.COMPLETED, null, null, null, null, null).size());
        assertEquals(1, service.listTrips(null, null, "2026-01-01", "2026-01-31", null, null).size());
        assertEquals(1, service.listTrips(null, null, null, null, null, null).size());

        ApiException exception = assertThrows(ApiException.class, () -> service.listTrips(null, null, null, null, null, 101));
        assertEquals(400, exception.getStatus());
    }

    @Test
    void updateTripStatusAndCancelShouldMutateLoadedTrip() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        Driver resolvedDriver = new Driver();
        resolvedDriver.id = 8L;
        when(driverService.getDriver(8L)).thenReturn(resolvedDriver);
        Trip existing = new Trip();
        existing.id = 50L;
        existing.customerName = "Old";
        when(tripRepository.findById(50L)).thenReturn(existing);
        TripService service = new TripService(driverService, tripRepository);

        Trip updates = new Trip();
        updates.customerName = "New";
        updates.driver = new Driver();
        updates.driver.id = 8L;

        Trip updated = service.updateTrip(50L, updates);
        TripStatusUpdateResponse statusResponse = service.updateStatus(50L, TripStatus.IN_PROGRESS);
        MessageResponse cancelResponse = service.cancelTrip(50L);

        assertEquals("New", updated.customerName);
        assertEquals(8L, updated.driver.id);
        assertEquals(TripStatus.CANCELLED, existing.status);
        assertEquals("Status updated", statusResponse.message());
        assertEquals("Trip cancelled", cancelResponse.message());
    }

    @Test
    void getTripShouldFailWhenMissing() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        when(tripRepository.findById(123L)).thenReturn(null);
        TripService service = new TripService(driverService, tripRepository);

        ApiException exception = assertThrows(ApiException.class, () -> service.getTrip(123L));
        assertEquals(404, exception.getStatus());
        assertEquals("Trip not found", exception.getMessage());
    }

    @Test
    void updateStatusShouldRejectNullStatus() {
        DriverService driverService = mock(DriverService.class);
        TripRepository tripRepository = mock(TripRepository.class);
        TripService service = new TripService(driverService, tripRepository);

        ApiException exception = assertThrows(ApiException.class, () -> service.updateStatus(1L, null));
        assertEquals(400, exception.getStatus());
    }

    @Test
    void tripApiMapperShouldMapRequestAndResponse() {
        TripRequest request = new TripRequest(
                "Alice",
                "123",
                "A",
                "B",
                LocalDate.of(2026, 2, 2),
                TripType.LOCAL,
                25.5,
                "note",
                9L
        );

        Trip trip = TripApiMapper.toEntity(request);
        trip.id = 70L;
        trip.tripCode = "TRIP-1";
        trip.status = TripStatus.SCHEDULED;
        trip.createdBy = "admin";
        trip.createdAt = LocalDateTime.of(2026, 2, 2, 10, 0);
        trip.driver.name = "Driver";
        trip.driver.phone = "555";
        trip.driver.status = DriverStatus.ACTIVE;

        TripResponse response = TripApiMapper.toResponse(trip);
        TripDriverResponse responseDriver = response.driver();

        assertNotNull(trip.driver);
        assertEquals(9L, trip.driver.id);
        assertEquals(70L, response.id());
        assertEquals("TRIP-1", response.tripCode());
        assertEquals("Driver", responseDriver.name());
        assertEquals(DriverStatus.ACTIVE, responseDriver.status());
        assertNull(TripApiMapper.toResponse(new Trip()).driver());
    }
}