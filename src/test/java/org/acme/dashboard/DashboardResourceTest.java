package org.acme.dashboard;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DashboardResourceTest {

    @InjectMock
    JsonWebToken jwt;

    @BeforeEach
    void setupJwt() {
        Mockito.when(jwt.getSubject()).thenReturn("dashboard-subject");
    }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void dashboardShouldReturnSummary() {
        Long tripId = createTrip();
        upsertFinancials(tripId);

        given()
                .when().get("/api/dashboard")
                .then()
                .statusCode(200)
                .body("totalTrips", notNullValue())
                .body("totalRevenue", notNullValue())
                .body("totalProfit", notNullValue());
    }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void financialSummaryReportShouldReturnSummary() {
        given()
                .when().get("/api/dashboard/reports/summary")
                .then()
                .statusCode(200)
                .body("totalTrips", notNullValue())
                .body("revenueThisMonth", notNullValue());
    }

    private Long createTrip() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "customerName", "Dash Customer",
                        "fromLocation", "A",
                        "toLocation", "B",
                        "tripDate", LocalDate.now().plusDays(1).toString()
                ))
                .when().post("/api/trips")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void upsertFinancials(Long tripId) {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("revenue", new BigDecimal("1200.00"), "fuelCost", new BigDecimal("200.00")))
                .when().post("/api/trips/" + tripId + "/financials")
                .then()
                .statusCode(201);
    }
}
