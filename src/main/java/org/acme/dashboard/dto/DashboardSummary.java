package org.acme.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummary {

    public long totalTrips;
    public long completedTrips;
    public long inProgressTrips;
    public long scheduledTrips;
    public long cancelledTrips;
    public long activeDrivers;
    public BigDecimal totalRevenue;
    public BigDecimal totalExpenses;
    public BigDecimal totalProfit;
    public long tripsThisMonth;
    public BigDecimal revenueThisMonth;
    public BigDecimal profitThisMonth;

    public DashboardSummary() {
        totalRevenue = BigDecimal.ZERO;
        totalExpenses = BigDecimal.ZERO;
        totalProfit = BigDecimal.ZERO;
        revenueThisMonth = BigDecimal.ZERO;
        profitThisMonth = BigDecimal.ZERO;
    }
}