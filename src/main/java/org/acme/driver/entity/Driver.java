package org.acme.driver.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Column(nullable = false)
    public String name;

    @NotBlank
    @Column(nullable = false)
    public String phone;

    @Column(name = "license_number", unique = true)
    public String licenseNumber;

    @Column(name = "license_expiry")
    public LocalDate licenseExpiry;

    @Column(name = "vehicle_number")
    public String vehicleNumber;

    @Column(name = "vehicle_type")
    public String vehicleType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DriverStatus status = DriverStatus.ACTIVE;

    @Column(name = "joined_date")
    public LocalDate joinedDate;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (joinedDate == null) {
            joinedDate = LocalDate.now();
        }
    }
}