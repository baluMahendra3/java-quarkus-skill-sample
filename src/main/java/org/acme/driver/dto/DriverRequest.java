package org.acme.driver.dto;

import jakarta.validation.constraints.NotBlank;
import org.acme.driver.entity.DriverStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "DriverRequest", description = "Payload used to create or update a driver profile")
public record DriverRequest(
        @NotBlank
        @Schema(description = "Driver full name", example = "Ravi Kumar")
        String name,
        @NotBlank
        @Schema(description = "Primary contact phone number", example = "9876543210")
        String phone,
        @Schema(description = "Government-issued or company-issued driving license number", example = "TN-09-2026-123456")
        String licenseNumber,
        @Schema(description = "Driver license expiry date in ISO-8601 format", example = "2027-12-31")
        LocalDate licenseExpiry,
        @Schema(description = "Assigned vehicle registration number", example = "TN01AB1234")
        String vehicleNumber,
        @Schema(description = "Vehicle category handled by the driver", example = "Mini Bus")
        String vehicleType,
        @Schema(description = "Operational status of the driver", example = "AVAILABLE")
        DriverStatus status
) {
}