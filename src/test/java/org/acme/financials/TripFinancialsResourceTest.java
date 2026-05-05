package org.acme.financials;

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class TripFinancialsResourceTest {

        @InjectMock
        JsonWebToken jwt;

        @BeforeEach
        void setupJwt() {
                Mockito.when(jwt.getSubject()).thenReturn("finance-subject");
        }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void upsertAndGetFinancials() {
        Long tripId = createTrip();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "revenue", new BigDecimal("5000.00"),
                        "fuelCost", new BigDecimal("1000.00"),
                        "tollCost", new BigDecimal("200.00")
                ))
                .when().post("/api/trips/" + tripId + "/financials")
                .then()
                .statusCode(201)
                .body("revenue", equalTo(5000.00f));

        given()
                .when().get("/api/trips/" + tripId + "/financials")
                .then()
                .statusCode(200)
                .body("netProfit", equalTo(3800.00f));
    }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void financialsShouldReturnNotFoundForUnknownTrip() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("revenue", 1000))
                .when().post("/api/trips/999999/financials")
                .then()
                .statusCode(404)
                .body("error", equalTo("Trip not found"));
    }

        @Test
        @TestSecurity(user = "mgr", roles = {"MANAGER"})
        void financialsShouldRejectNegativeAmounts() {
                Long tripId = createTrip();

                given()
                                .contentType(ContentType.JSON)
                                .body(Map.of("revenue", 1000, "fuelCost", -1))
                                .when().post("/api/trips/" + tripId + "/financials")
                                .then()
                                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must be greater than or equal to 0")));
        }

    private Long createTrip() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "customerName", "Finance Customer",
                        "fromLocation", "Coimbatore",
                        "toLocation", "Madurai",
                        "tripDate", LocalDate.now().plusDays(3).toString()
                ))
                .when().post("/api/trips")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
