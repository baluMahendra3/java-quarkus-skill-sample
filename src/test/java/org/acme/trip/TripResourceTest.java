package org.acme.trip;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class TripResourceTest {

        @InjectMock
        JsonWebToken jwt;

        @BeforeEach
        void setupJwt() {
                Mockito.when(jwt.getSubject()).thenReturn("test-subject");
        }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void createTripAndUpdateStatus() {
        Long driverId = createDriver();

        Long tripId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "customerName", "Customer A",
                        "fromLocation", "Chennai",
                        "toLocation", "Bengaluru",
                        "tripDate", LocalDate.now().plusDays(1).toString(),
                        "driverId", driverId
                ))
                .when().post("/api/trips")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("SCHEDULED"))
                .extract().jsonPath().getLong("id");

        given()
                .queryParam("status", "IN_PROGRESS")
                .when().put("/api/trips/" + tripId + "/status")
                .then()
                .statusCode(200)
                .body("message", equalTo("Status updated"))
                .body("status", equalTo("IN_PROGRESS"));
    }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void createTripShouldValidateRequiredFields() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("customerName", "Missing Fields"))
                .when().post("/api/trips")
                .then()
                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must not be blank")));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void cancelTripShouldSetCancelledStatus() {
        Long tripId = createTripWithoutDriver();

        given()
                .when().delete("/api/trips/" + tripId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Trip cancelled"));
    }

        @Test
        @TestSecurity(user = "mgr", roles = {"MANAGER"})
        void listTripsShouldSupportPagination() {
                createTripWithoutDriver("Paged Customer 1");
                createTripWithoutDriver("Paged Customer 2");

                given()
                                .queryParam("page", 0)
                                .queryParam("size", 1)
                                .when().get("/api/trips")
                                .then()
                                .statusCode(200)
                                .body("size()", equalTo(1));
        }

        @Test
        @TestSecurity(user = "mgr", roles = {"MANAGER"})
        void listTripsShouldRejectInvalidPage() {
                given()
                                .queryParam("page", -1)
                                .when().get("/api/trips")
                                .then()
                                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must be greater than or equal to 0")));
        }

    private Long createDriver() {
                return given()
                                .contentType(ContentType.JSON)
                                .body(Map.of("name", "Trip Driver", "phone", "7777777777"))
                                .when().post("/api/drivers")
                                .then()
                                .statusCode(201)
                                .extract().jsonPath().getLong("id");
    }

    private Long createTripWithoutDriver() {
        return createTripWithoutDriver("Customer B");
    }

    private Long createTripWithoutDriver(String customerName) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "customerName", customerName,
                        "fromLocation", "Pune",
                        "toLocation", "Mumbai",
                        "tripDate", LocalDate.now().plusDays(2).toString()
                ))
                .when().post("/api/trips")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
