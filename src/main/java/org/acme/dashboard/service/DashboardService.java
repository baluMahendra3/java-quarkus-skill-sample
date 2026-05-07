package org.acme.dashboard.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dashboard.entity.DashboardMetrics;
import org.acme.dashboard.repository.DashboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@ApplicationScoped
public class DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardMetrics getDashboard() {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        DashboardMetrics metrics = dashboardRepository.loadDashboardMetrics(firstOfMonth, today);
        LOG.info("event=dashboard.summary.completed from={} to={}", firstOfMonth, today);
        return metrics;
    }
}