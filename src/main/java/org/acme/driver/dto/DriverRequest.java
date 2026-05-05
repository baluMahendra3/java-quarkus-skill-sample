package org.acme.driver.dto;

import jakarta.validation.constraints.NotBlank;
import org.acme.driver.entity.DriverStatus;

import java.time.LocalDate;

public record DriverRequest(
        @NotBlank
        String name,
        @NotBlank
        String phone,
        String licenseNumber,
        LocalDate licenseExpiry,
        String vehicleNumber,
        String vehicleType,
        DriverStatus status
) {
}