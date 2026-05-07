package org.acme.driver.dto;

import org.acme.driver.entity.DriverStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "DriverResponse", description = "Driver details returned by the API")
public record DriverResponse(
        @Schema(description = "Unique driver identifier", example = "101")
        Long id,
        @Schema(description = "Driver full name", example = "Ravi Kumar")
        String name,
        @Schema(description = "Primary contact phone number", example = "9876543210")
        String phone,
        @Schema(description = "Driving license number", example = "TN-09-2026-123456")
        String licenseNumber,
        @Schema(description = "Driver license expiry date", example = "2027-12-31")
        LocalDate licenseExpiry,
        @Schema(description = "Assigned vehicle registration number", example = "TN01AB1234")
        String vehicleNumber,
        @Schema(description = "Assigned vehicle type", example = "Mini Bus")
        String vehicleType,
        @Schema(description = "Current operational status", example = "AVAILABLE")
        DriverStatus status,
        @Schema(description = "Date the driver joined the company", example = "2025-04-01")
        LocalDate joinedDate,
        @Schema(description = "Timestamp when the driver record was created", example = "2026-05-06T00:15:30")
        LocalDateTime createdAt
) {
}