package org.acme.financials.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.acme.financials.entity.TripFinancials;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public class TripFinancialsRepository implements PanacheRepository<TripFinancials> {

    @PersistenceContext
    EntityManager entityManager;

    public Optional<TripFinancials> findByTripId(Long tripId) {
        return find("trip.id", tripId).firstResultOptional();
    }

    public BigDecimal[] aggregateTotals() {
        Object[] row = (Object[]) entityManager
                .createQuery(
                        "SELECT SUM(f.revenue), " +
                        "SUM(f.fuelCost + f.tollCost + f.driverAllowance + f.otherExpenses), " +
                        "SUM(f.netProfit) FROM TripFinancials f")
                .getSingleResult();
        BigDecimal rev = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
        BigDecimal exp = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
        BigDecimal profit = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        return new BigDecimal[]{rev, exp, profit};
    }

    public BigDecimal[] aggregateMonthlyTotals(LocalDate from, LocalDate to) {
        Object[] row = (Object[]) entityManager
                .createQuery(
                        "SELECT SUM(f.revenue), SUM(f.netProfit) FROM TripFinancials f " +
                                "WHERE f.trip.tripDate >= :from AND f.trip.tripDate <= :to")
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        BigDecimal revenue = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
        BigDecimal profit = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
        return new BigDecimal[]{revenue, profit};
    }
}