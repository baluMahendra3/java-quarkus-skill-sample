package org.acme.driver.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;
import org.acme.driver.repository.DriverRepository;

import java.util.List;

@ApplicationScoped
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Driver createDriver(Driver driver) {
        if (driver.name == null || driver.phone == null) {
            throw new ApiException(400, "name and phone are required");
        }
        driverRepository.persist(driver);
        return driver;
    }

    public List<Driver> listDrivers(DriverStatus status, Integer page, Integer size) {
        int pageIndex = resolvePage(page);
        int pageSize = resolveSize(size);
        if (status != null) {
            return driverRepository.findByStatus(status, pageIndex, pageSize);
        }
        return driverRepository.listDrivers(pageIndex, pageSize);
    }

    public Driver getDriver(Long id) {
        Driver driver = driverRepository.findById(id);
        if (driver == null) {
            throw new ApiException(404, "Driver not found");
        }
        return driver;
    }

    @Transactional
    public Driver updateDriver(Long id, Driver updated) {
        Driver driver = getDriver(id);
        if (updated.name != null) driver.name = updated.name;
        if (updated.phone != null) driver.phone = updated.phone;
        if (updated.licenseNumber != null) driver.licenseNumber = updated.licenseNumber;
        if (updated.licenseExpiry != null) driver.licenseExpiry = updated.licenseExpiry;
        if (updated.vehicleNumber != null) driver.vehicleNumber = updated.vehicleNumber;
        if (updated.vehicleType != null) driver.vehicleType = updated.vehicleType;
        if (updated.status != null) driver.status = updated.status;
        return driver;
    }

    @Transactional
    public MessageResponse deactivateDriver(Long id) {
        Driver driver = getDriver(id);
        driver.status = DriverStatus.INACTIVE;
        return new MessageResponse("Driver deactivated");
    }

    public long countActiveDrivers() {
        return driverRepository.countActive();
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