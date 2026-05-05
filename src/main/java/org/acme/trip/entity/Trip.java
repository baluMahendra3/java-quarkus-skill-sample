package org.acme.trip.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.acme.driver.entity.Driver;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "trip_code", unique = true)
    public String tripCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    public Driver driver;

    @NotBlank
    @Column(name = "customer_name", nullable = false)
    public String customerName;

    @Column(name = "customer_phone")
    public String customerPhone;

    @NotBlank
    @Column(name = "from_location", nullable = false)
    public String fromLocation;

    @NotBlank
    @Column(name = "to_location", nullable = false)
    public String toLocation;

    @NotNull
    @Column(name = "trip_date", nullable = false)
    public LocalDate tripDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type")
    public TripType tripType = TripType.LOCAL;

    @Column(name = "distance_km")
    public Double distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TripStatus status = TripStatus.SCHEDULED;

    @Column(name = "notes")
    public String notes;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (tripCode == null || tripCode.isBlank()) {
            tripCode = "TRP-" + System.currentTimeMillis();
        }
    }
}