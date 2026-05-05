package org.acme.common;

import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RegisterRequest;
import org.acme.dashboard.dto.DashboardSummary;
import org.acme.driver.entity.DriverStatus;
import org.acme.trip.entity.TripStatus;
import org.acme.trip.entity.TripType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumAndDtoClassesTest {

    @Test
    void roleEnumShouldExposeExpectedValues() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.MANAGER, Role.valueOf("MANAGER"));
        assertEquals(Role.DRIVER, Role.valueOf("DRIVER"));
    }

    @Test
    void statusEnumsShouldExposeExpectedValues() {
        assertEquals(DriverStatus.ACTIVE, DriverStatus.valueOf("ACTIVE"));
        assertEquals(TripStatus.SCHEDULED, TripStatus.valueOf("SCHEDULED"));
        assertEquals(TripType.LOCAL, TripType.valueOf("LOCAL"));
    }

    @Test
    void loginAndRegisterDtosShouldAllowFieldAssignment() {
        LoginRequest login = new LoginRequest();
        login.email = "a@b.com";
        login.password = "p";

        RegisterRequest register = new RegisterRequest();
        register.name = "A";
        register.email = "a@b.com";
        register.password = "p";
        register.role = Role.MANAGER;

        assertEquals("a@b.com", login.email);
        assertEquals("p", login.password);
        assertEquals("A", register.name);
        assertEquals(Role.MANAGER, register.role);
    }

    @Test
    void dashboardSummaryShouldInitializeMonetaryFields() {
        DashboardSummary summary = new DashboardSummary();
        assertEquals(BigDecimal.ZERO, summary.totalRevenue);
        assertEquals(BigDecimal.ZERO, summary.totalExpenses);
        assertEquals(BigDecimal.ZERO, summary.totalProfit);
        assertEquals(BigDecimal.ZERO, summary.revenueThisMonth);
        assertEquals(BigDecimal.ZERO, summary.profitThisMonth);
        assertEquals(0L, summary.totalTrips);
    }
}
