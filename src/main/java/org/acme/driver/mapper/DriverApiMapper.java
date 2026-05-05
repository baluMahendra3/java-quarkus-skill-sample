package org.acme.driver.mapper;

import org.acme.driver.dto.DriverRequest;
import org.acme.driver.dto.DriverResponse;
import org.acme.driver.entity.Driver;

public final class DriverApiMapper {

    private DriverApiMapper() {
    }

    public static Driver toEntity(DriverRequest request) {
        Driver driver = new Driver();
        driver.name = request.name();
        driver.phone = request.phone();
        driver.licenseNumber = request.licenseNumber();
        driver.licenseExpiry = request.licenseExpiry();
        driver.vehicleNumber = request.vehicleNumber();
        driver.vehicleType = request.vehicleType();
        if (request.status() != null) {
            driver.status = request.status();
        }
        return driver;
    }

    public static DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.id,
                driver.name,
                driver.phone,
                driver.licenseNumber,
                driver.licenseExpiry,
                driver.vehicleNumber,
                driver.vehicleType,
                driver.status,
                driver.joinedDate,
                driver.createdAt
        );
    }
}