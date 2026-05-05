package org.acme.model;

import org.acme.auth.entity.User;
import org.acme.common.Role;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;
import org.acme.financials.entity.TripFinancials;
import org.acme.trip.entity.Trip;
import org.acme.trip.entity.TripStatus;
import org.acme.trip.entity.TripType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityLifecycleTest {

    @Test
    void driverPrePersistShouldInitializeDates() {
        Driver driver = new Driver();
        driver.name = "D";
        driver.phone = "123";

        driver.prePersist();

        assertNotNull(driver.createdAt);
        assertNotNull(driver.joinedDate);
        assertEquals(DriverStatus.ACTIVE, driver.status);
    }

    @Test
    void tripPrePersistShouldInitializeAuditAndCode() {
        Trip trip = new Trip();
        trip.customerName = "C";
        trip.fromLocation = "X";
        trip.toLocation = "Y";
        trip.tripDate = LocalDate.now();

        trip.prePersist();

        assertNotNull(trip.createdAt);
        assertNotNull(trip.tripCode);
        assertTrue(trip.tripCode.startsWith("TRP-"));
        assertEquals(TripStatus.SCHEDULED, trip.status);
        assertEquals(TripType.LOCAL, trip.tripType);
    }

    @Test
    void tripFinancialsComputeShouldSetProfit() {
        TripFinancials financials = new TripFinancials();
        financials.revenue = new BigDecimal("1000.00");
        financials.fuelCost = new BigDecimal("100.00");
        financials.tollCost = new BigDecimal("50.00");
        financials.driverAllowance = new BigDecimal("150.00");
        financials.otherExpenses = new BigDecimal("25.00");

        financials.computeAndStamp();

        assertNotNull(financials.createdAt);
        assertEquals(new BigDecimal("675.00"), financials.netProfit);
    }

    @Test
    void userPrePersistShouldSetCreatedAtAndDefaults() {
        User user = new User();
        user.name = "User";
        user.email = "u@test.com";
        user.password = "hashed";
        user.role = Role.MANAGER;

        user.prePersist();

        assertNotNull(user.createdAt);
        assertTrue(user.active);
    }
}
