package org.acme.financials.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.acme.trip.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_financials")
public class TripFinancials extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", unique = true, nullable = false)
    public Trip trip;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "fuel_cost", precision = 12, scale = 2)
    public BigDecimal fuelCost = BigDecimal.ZERO;

    @Column(name = "toll_cost", precision = 12, scale = 2)
    public BigDecimal tollCost = BigDecimal.ZERO;

    @Column(name = "driver_allowance", precision = 12, scale = 2)
    public BigDecimal driverAllowance = BigDecimal.ZERO;

    @Column(name = "other_expenses", precision = 12, scale = 2)
    public BigDecimal otherExpenses = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 12, scale = 2)
    public BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void computeAndStamp() {
        createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        BigDecimal totalExpenses = fuelCost
                .add(tollCost)
                .add(driverAllowance)
                .add(otherExpenses);
        netProfit = revenue.subtract(totalExpenses);
    }
}