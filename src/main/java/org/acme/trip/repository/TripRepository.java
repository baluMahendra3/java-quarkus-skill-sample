package org.acme.trip.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trip.entity.Trip;
import org.acme.trip.entity.TripStatus;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TripRepository implements PanacheRepository<Trip> {

    public List<Trip> findByDriver(Long driverId) {
        return list("driver.id", driverId);
    }

    public List<Trip> findByDriver(Long driverId, int pageIndex, int pageSize) {
        return find("driver.id", driverId)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public List<Trip> findByStatus(TripStatus status) {
        return list("status", status);
    }

    public List<Trip> findByStatus(TripStatus status, int pageIndex, int pageSize) {
        return find("status", status)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public List<Trip> findByDateRange(LocalDate from, LocalDate to) {
        return list("tripDate >= ?1 and tripDate <= ?2", from, to);
    }

    public List<Trip> findByDateRange(LocalDate from, LocalDate to, int pageIndex, int pageSize) {
        return find("tripDate >= ?1 and tripDate <= ?2", from, to)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public List<Trip> listTrips(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public long countByStatus(TripStatus status) {
        return count("status", status);
    }
}