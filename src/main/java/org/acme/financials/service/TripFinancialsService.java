package org.acme.financials.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.common.error.ApiException;
import org.acme.financials.entity.TripFinancials;
import org.acme.financials.repository.TripFinancialsRepository;
import org.acme.trip.entity.Trip;
import org.acme.trip.service.TripService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class TripFinancialsService {

    private static final Logger LOG = LoggerFactory.getLogger(TripFinancialsService.class);

    private final TripService tripService;
    private final TripFinancialsRepository tripFinancialsRepository;

    public TripFinancialsService(TripService tripService, TripFinancialsRepository tripFinancialsRepository) {
        this.tripService = tripService;
        this.tripFinancialsRepository = tripFinancialsRepository;
    }

    @Transactional
    public TripFinancials upsertFinancials(Long tripId, TripFinancials data) {
        Trip trip = resolveTrip(tripId);

        Optional<TripFinancials> existing = tripFinancialsRepository.findByTripId(tripId);
        boolean created = existing.isEmpty();
        TripFinancials financials = existing.orElse(new TripFinancials());
        financials.trip = trip;

        if (data.revenue != null) financials.revenue = data.revenue;
        if (data.fuelCost != null) financials.fuelCost = data.fuelCost;
        if (data.tollCost != null) financials.tollCost = data.tollCost;
        if (data.driverAllowance != null) financials.driverAllowance = data.driverAllowance;
        if (data.otherExpenses != null) financials.otherExpenses = data.otherExpenses;

        if (created) {
            tripFinancialsRepository.persist(financials);
        }
        LOG.info("event=trip.financials.upsert.completed tripId={} action={}", tripId, created ? "create" : "update");
        return financials;
    }

    public Optional<TripFinancials> findFinancials(Long tripId) {
        return tripFinancialsRepository.findByTripId(tripId);
    }

    public TripFinancials getFinancials(Long tripId) {
        Optional<TripFinancials> financials = findFinancials(tripId);
        if (financials.isEmpty()) {
            LOG.warn("event=trip.financials.get.notFound tripId={}", tripId);
            throw new ApiException(404, "No financials recorded for this trip");
        }
        LOG.info("event=trip.financials.get.completed tripId={}", tripId);
        return financials.get();
    }

    private Trip resolveTrip(Long tripId) {
        try {
            return tripService.getTrip(tripId);
        }
        catch (ApiException exception) {
            LOG.warn("event=trip.financials.tripNotFound tripId={}", tripId);
            throw new ApiException(404, "Trip not found");
        }
    }
}