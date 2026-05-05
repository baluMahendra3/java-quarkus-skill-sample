package org.acme.driver.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;

import java.util.List;

@ApplicationScoped
public class DriverRepository implements PanacheRepository<Driver> {

    public List<Driver> findByStatus(DriverStatus status) {
        return list("status", status);
    }

    public List<Driver> findByStatus(DriverStatus status, int pageIndex, int pageSize) {
        return find("status", status)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public List<Driver> listDrivers(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public long countActive() {
        return count("status", DriverStatus.ACTIVE);
    }
}