package org.acme.driver.dto;

import org.acme.driver.entity.DriverStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DriverResponse(
        Long id,
        String name,
        String phone,
        String licenseNumber,
        LocalDate licenseExpiry,
        String vehicleNumber,
        String vehicleType,
        DriverStatus status,
        LocalDate joinedDate,
        LocalDateTime createdAt
) {
}