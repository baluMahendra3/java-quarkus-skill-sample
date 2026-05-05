package org.acme.dashboard.entity;

import java.math.BigDecimal;

public class DashboardMetrics {

    public long totalTrips;
    public long completedTrips;
    public long inProgressTrips;
    public long scheduledTrips;
    public long cancelledTrips;
    public long activeDrivers;
    public BigDecimal totalRevenue = BigDecimal.ZERO;
    public BigDecimal totalExpenses = BigDecimal.ZERO;
    public BigDecimal totalProfit = BigDecimal.ZERO;
    public long tripsThisMonth;
    public BigDecimal revenueThisMonth = BigDecimal.ZERO;
    public BigDecimal profitThisMonth = BigDecimal.ZERO;
}