package org.acme.common.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorClassesTest {

    @Test
    void apiExceptionShouldExposeStatusAndMessage() {
        ApiException ex = new ApiException(409, "Conflict");
        assertEquals(409, ex.getStatus());
        assertEquals("Conflict", ex.getMessage());
    }

    @Test
    void errorResponseConstructorShouldAssignFields() {
        ErrorResponse response = new ErrorResponse("Bad request", 400, "/api/test", "2026-01-01T00:00:00Z", List.of("d1"));
        assertEquals("Bad request", response.error);
        assertEquals(400, response.status);
        assertEquals("/api/test", response.path);
        assertEquals("2026-01-01T00:00:00Z", response.timestamp);
        assertEquals(1, response.details.size());
    }

    @Test
    void errorResponseNoArgConstructorShouldLeaveDefaults() {
        ErrorResponse response = new ErrorResponse();
        assertNull(response.error);
        assertEquals(0, response.status);
        assertNull(response.path);
    }
}
