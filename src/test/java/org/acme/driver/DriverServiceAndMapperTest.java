package org.acme.driver;

import org.acme.common.dto.MessageResponse;
import org.acme.common.error.ApiException;
import org.acme.driver.dto.DriverRequest;
import org.acme.driver.dto.DriverResponse;
import org.acme.driver.entity.Driver;
import org.acme.driver.entity.DriverStatus;
import org.acme.driver.mapper.DriverApiMapper;
import org.acme.driver.repository.DriverRepository;
import org.acme.driver.service.DriverService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverServiceAndMapperTest {

    @Test
    void createDriverShouldPersistValidDriver() {
        DriverRepository repository = mock(DriverRepository.class);
        doAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            driver.id = 5L;
            return null;
        }).when(repository).persist(any(Driver.class));
        DriverService service = new DriverService(repository);

        Driver driver = new Driver();
        driver.name = "Driver One";
        driver.phone = "123";

        Driver created = service.createDriver(driver);

        verify(repository).persist(driver);
        assertEquals(5L, created.id);
    }

    @Test
    void createDriverShouldRejectMissingFields() {
        DriverRepository repository = mock(DriverRepository.class);
        DriverService service = new DriverService(repository);

        ApiException exception = assertThrows(ApiException.class, () -> service.createDriver(new Driver()));
        assertEquals(400, exception.getStatus());
    }

    @Test
    void listDriversShouldUseStatusFilterAndValidatePage() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findByStatus(DriverStatus.ACTIVE, 1, 20)).thenReturn(List.of(new Driver()));
        DriverService service = new DriverService(repository);

        assertEquals(1, service.listDrivers(DriverStatus.ACTIVE, 1, 20).size());
        verify(repository).findByStatus(DriverStatus.ACTIVE, 1, 20);

        ApiException exception = assertThrows(ApiException.class, () -> service.listDrivers(null, -1, 20));
        assertEquals(400, exception.getStatus());
    }

    @Test
    void updateAndDeactivateDriverShouldMutateLoadedDriver() {
        DriverRepository repository = mock(DriverRepository.class);
        Driver existing = new Driver();
        existing.id = 11L;
        existing.name = "Old";
        existing.phone = "111";
        existing.status = DriverStatus.ACTIVE;
        when(repository.findById(11L)).thenReturn(existing);
        when(repository.countActive()).thenReturn(3L);
        DriverService service = new DriverService(repository);

        Driver updates = new Driver();
        updates.name = "New";
        updates.phone = "222";
        updates.status = DriverStatus.INACTIVE;

        Driver updated = service.updateDriver(11L, updates);
        MessageResponse deactivated = service.deactivateDriver(11L);

        assertEquals("New", updated.name);
        assertEquals("222", updated.phone);
        assertEquals(DriverStatus.INACTIVE, existing.status);
        assertEquals("Driver deactivated", deactivated.message());
        assertEquals(3L, service.countActiveDrivers());
    }

    @Test
    void getDriverAndListDriversShouldHandleDefaultsAndMissingDriver() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findById(99L)).thenReturn(null);
        when(repository.listDrivers(0, 50)).thenReturn(List.of(new Driver()));
        DriverService service = new DriverService(repository);

        assertEquals(1, service.listDrivers(null, null, null).size());
        verify(repository).listDrivers(0, 50);

        ApiException exception = assertThrows(ApiException.class, () -> service.getDriver(99L));
        assertEquals(404, exception.getStatus());
        assertEquals("Driver not found", exception.getMessage());
    }

    @Test
    void driverApiMapperShouldMapRequestAndResponse() {
        DriverRequest request = new DriverRequest(
                "Alice",
                "123",
                "LIC-1",
                LocalDate.of(2026, 2, 1),
                "VH-1",
                "Truck",
                DriverStatus.ACTIVE
        );

        Driver entity = DriverApiMapper.toEntity(request);
        entity.id = 9L;
        entity.joinedDate = LocalDate.of(2026, 1, 1);
        entity.createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        DriverResponse response = DriverApiMapper.toResponse(entity);

        assertEquals("Alice", entity.name);
        assertEquals(DriverStatus.ACTIVE, entity.status);
        assertEquals(9L, response.id());
        assertEquals("Alice", response.name());
        assertEquals(LocalDate.of(2026, 1, 1), response.joinedDate());
    }
}