package org.acme.dashboard.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dashboard.entity.DashboardMetrics;
import org.acme.dashboard.repository.DashboardRepository;

import java.time.LocalDate;

@ApplicationScoped
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardMetrics getDashboard() {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        return dashboardRepository.loadDashboardMetrics(firstOfMonth, today);
    }
}