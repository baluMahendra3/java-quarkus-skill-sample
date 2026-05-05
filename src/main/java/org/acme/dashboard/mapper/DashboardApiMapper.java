package org.acme.dashboard.mapper;

import org.acme.dashboard.dto.DashboardSummary;
import org.acme.dashboard.entity.DashboardMetrics;

public final class DashboardApiMapper {

    private DashboardApiMapper() {
    }

    public static DashboardSummary toResponse(DashboardMetrics metrics) {
        DashboardSummary summary = new DashboardSummary();
        summary.totalTrips = metrics.totalTrips;
        summary.completedTrips = metrics.completedTrips;
        summary.inProgressTrips = metrics.inProgressTrips;
        summary.scheduledTrips = metrics.scheduledTrips;
        summary.cancelledTrips = metrics.cancelledTrips;
        summary.activeDrivers = metrics.activeDrivers;
        summary.totalRevenue = metrics.totalRevenue;
        summary.totalExpenses = metrics.totalExpenses;
        summary.totalProfit = metrics.totalProfit;
        summary.tripsThisMonth = metrics.tripsThisMonth;
        summary.revenueThisMonth = metrics.revenueThisMonth;
        summary.profitThisMonth = metrics.profitThisMonth;
        return summary;
    }
}