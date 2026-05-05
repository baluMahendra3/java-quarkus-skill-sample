package org.acme.financials;

import org.acme.common.error.ApiException;
import org.acme.financials.dto.TripFinancialsRequest;
import org.acme.financials.dto.TripFinancialsResponse;
import org.acme.financials.entity.TripFinancials;
import org.acme.financials.mapper.TripFinancialsApiMapper;
import org.acme.financials.repository.TripFinancialsRepository;
import org.acme.financials.service.TripFinancialsService;
import org.acme.trip.entity.Trip;
import org.acme.trip.service.TripService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripFinancialsServiceAndMapperTest {

    @Test
    void upsertFinancialsShouldCreateAndPersistWhenMissing() {
        TripService tripService = mock(TripService.class);
        TripFinancialsRepository repository = mock(TripFinancialsRepository.class);
        Trip trip = new Trip();
        trip.id = 5L;
        when(tripService.getTrip(5L)).thenReturn(trip);
        when(repository.findByTripId(5L)).thenReturn(Optional.empty());
        doAnswer(invocation -> null).when(repository).persist(any(TripFinancials.class));
        TripFinancialsService service = new TripFinancialsService(tripService, repository);

        TripFinancials payload = new TripFinancials();
        payload.revenue = BigDecimal.valueOf(100);
        payload.fuelCost = BigDecimal.valueOf(25);

        TripFinancials financials = service.upsertFinancials(5L, payload);

        verify(repository).persist(financials);
        assertEquals(5L, financials.trip.id);
        assertEquals(BigDecimal.valueOf(100), financials.revenue);
        assertEquals(BigDecimal.valueOf(25), financials.fuelCost);
    }

    @Test
    void getFinancialsShouldFailWhenMissing() {
        TripService tripService = mock(TripService.class);
        TripFinancialsRepository repository = mock(TripFinancialsRepository.class);
        when(repository.findByTripId(8L)).thenReturn(Optional.empty());
        TripFinancialsService service = new TripFinancialsService(tripService, repository);

        ApiException exception = assertThrows(ApiException.class, () -> service.getFinancials(8L));
        assertEquals(404, exception.getStatus());
        assertEquals("No financials recorded for this trip", exception.getMessage());
    }

    @Test
    void upsertFinancialsShouldUpdateExistingAndHandleMissingTrip() {
        TripService tripService = mock(TripService.class);
        TripFinancialsRepository repository = mock(TripFinancialsRepository.class);
        Trip trip = new Trip();
        trip.id = 6L;
        when(tripService.getTrip(6L)).thenReturn(trip);
        TripFinancials existing = new TripFinancials();
        existing.trip = trip;
        existing.revenue = BigDecimal.TEN;
        when(repository.findByTripId(6L)).thenReturn(Optional.of(existing));
        TripFinancialsService service = new TripFinancialsService(tripService, repository);

        TripFinancials data = new TripFinancials();
        data.otherExpenses = BigDecimal.ONE;

        TripFinancials updated = service.upsertFinancials(6L, data);
        assertEquals(BigDecimal.ONE, updated.otherExpenses);

        when(tripService.getTrip(7L)).thenThrow(new ApiException(404, "Trip missing"));
        ApiException exception = assertThrows(ApiException.class, () -> service.upsertFinancials(7L, new TripFinancials()));
        assertEquals(404, exception.getStatus());
        assertEquals("Trip not found", exception.getMessage());
    }

    @Test
    void tripFinancialsApiMapperShouldMapRequestAndResponse() {
        TripFinancialsRequest request = new TripFinancialsRequest(
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(5)
        );

        TripFinancials entity = TripFinancialsApiMapper.toEntity(request);
        entity.id = 4L;
        entity.trip = new Trip();
        entity.trip.id = 7L;
        entity.netProfit = BigDecimal.valueOf(115);
        entity.createdAt = LocalDateTime.of(2026, 3, 1, 9, 0);

        TripFinancialsResponse response = TripFinancialsApiMapper.toResponse(entity);

        assertEquals(BigDecimal.valueOf(200), entity.revenue);
        assertEquals(4L, response.id());
        assertEquals(7L, response.tripId());
        assertEquals(BigDecimal.valueOf(115), response.netProfit());
    }
}