package org.acme.dashboard;

import org.acme.dashboard.dto.DashboardSummary;
import org.acme.dashboard.entity.DashboardMetrics;
import org.acme.dashboard.mapper.DashboardApiMapper;
import org.acme.dashboard.repository.DashboardRepository;
import org.acme.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceAndMapperTest {

    @Test
    void getDashboardShouldLoadMetricsForCurrentMonth() {
        DashboardRepository repository = mock(DashboardRepository.class);
        DashboardMetrics metrics = new DashboardMetrics();
        when(repository.loadDashboardMetrics(any(LocalDate.class), any(LocalDate.class))).thenReturn(metrics);
        DashboardService service = new DashboardService(repository);

        DashboardMetrics response = service.getDashboard();

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(repository).loadDashboardMetrics(fromCaptor.capture(), toCaptor.capture());
        assertEquals(1, fromCaptor.getValue().getDayOfMonth());
        assertTrue(!toCaptor.getValue().isBefore(fromCaptor.getValue()));
        assertEquals(metrics, response);
    }

    @Test
    void dashboardApiMapperShouldCopyMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();
        metrics.totalTrips = 10L;
        metrics.completedTrips = 5L;
        metrics.activeDrivers = 3L;
        metrics.totalRevenue = BigDecimal.valueOf(1000);
        metrics.totalExpenses = BigDecimal.valueOf(400);
        metrics.totalProfit = BigDecimal.valueOf(600);
        metrics.tripsThisMonth = 2L;
        metrics.revenueThisMonth = BigDecimal.valueOf(250);
        metrics.profitThisMonth = BigDecimal.valueOf(150);

        DashboardSummary summary = DashboardApiMapper.toResponse(metrics);

        assertEquals(10L, summary.totalTrips);
        assertEquals(5L, summary.completedTrips);
        assertEquals(3L, summary.activeDrivers);
        assertEquals(BigDecimal.valueOf(1000), summary.totalRevenue);
        assertEquals(BigDecimal.valueOf(150), summary.profitThisMonth);
    }
}