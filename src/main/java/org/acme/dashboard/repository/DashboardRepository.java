package org.acme.dashboard.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.acme.dashboard.entity.DashboardMetrics;
import org.acme.driver.entity.DriverStatus;
import org.acme.trip.entity.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@ApplicationScoped
public class DashboardRepository {

    @PersistenceContext
    EntityManager entityManager;

    public DashboardMetrics loadDashboardMetrics(LocalDate from, LocalDate to) {
        DashboardMetrics metrics = new DashboardMetrics();
        metrics.totalTrips = countTrips();
        metrics.completedTrips = countTripsByStatus(TripStatus.COMPLETED);
        metrics.inProgressTrips = countTripsByStatus(TripStatus.IN_PROGRESS);
        metrics.scheduledTrips = countTripsByStatus(TripStatus.SCHEDULED);
        metrics.cancelledTrips = countTripsByStatus(TripStatus.CANCELLED);
        metrics.activeDrivers = countActiveDrivers();

        BigDecimal[] totals = aggregateTotals();
        metrics.totalRevenue = totals[0];
        metrics.totalExpenses = totals[1];
        metrics.totalProfit = totals[2];

        metrics.tripsThisMonth = countTripsInRange(from, to);

        BigDecimal[] monthlyTotals = aggregateMonthlyTotals(from, to);
        metrics.revenueThisMonth = monthlyTotals[0];
        metrics.profitThisMonth = monthlyTotals[1];
        return metrics;
    }

    private long countTrips() {
        return (Long) entityManager.createQuery("SELECT COUNT(t) FROM Trip t").getSingleResult();
    }

    private long countTripsByStatus(TripStatus status) {
        return (Long) entityManager
                .createQuery("SELECT COUNT(t) FROM Trip t WHERE t.status = :status")
                .setParameter("status", status)
                .getSingleResult();
    }

    private long countActiveDrivers() {
        return (Long) entityManager
                .createQuery("SELECT COUNT(d) FROM Driver d WHERE d.status = :status")
                .setParameter("status", DriverStatus.ACTIVE)
                .getSingleResult();
    }

    private long countTripsInRange(LocalDate from, LocalDate to) {
        return (Long) entityManager
                .createQuery("SELECT COUNT(t) FROM Trip t WHERE t.tripDate >= :from AND t.tripDate <= :to")
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    private BigDecimal[] aggregateTotals() {
        Object[] row = (Object[]) entityManager
                .createQuery(
                        "SELECT SUM(f.revenue), " +
                                "SUM(f.fuelCost + f.tollCost + f.driverAllowance + f.otherExpenses), " +
                                "SUM(f.netProfit) FROM TripFinancials f")
                .getSingleResult();
        BigDecimal revenue = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
        BigDecimal expenses = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
        BigDecimal profit = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        return new BigDecimal[]{revenue, expenses, profit};
    }

    private BigDecimal[] aggregateMonthlyTotals(LocalDate from, LocalDate to) {
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