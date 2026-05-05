package org.acme.driver;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DriverResourceTest {

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void createAndGetDriver() {
        Long driverId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Driver A", "phone", "9999999999"))
                .when().post("/api/drivers")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("ACTIVE"))
                .extract().jsonPath().getLong("id");

        given()
                .when().get("/api/drivers/" + driverId)
                .then()
                .statusCode(200)
                .body("id", equalTo(driverId.intValue()))
                .body("name", equalTo("Driver A"));
    }

    @Test
    @TestSecurity(user = "mgr", roles = {"MANAGER"})
    void createDriverShouldValidateRequiredFields() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Only Name"))
                .when().post("/api/drivers")
                .then()
                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must not be blank")));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void deactivateDriverShouldWorkForAdmin() {
        Long driverId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Driver B", "phone", "8888888888"))
                .when().post("/api/drivers")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .when().delete("/api/drivers/" + driverId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Driver deactivated"));
    }

        @Test
        @TestSecurity(user = "mgr", roles = {"MANAGER"})
        void listDriversShouldSupportPagination() {
                createDriver("Driver C", "7777777701");
                createDriver("Driver D", "7777777702");

                given()
                                .queryParam("page", 0)
                                .queryParam("size", 1)
                                .when().get("/api/drivers")
                                .then()
                                .statusCode(200)
                                .body("size()", equalTo(1));
        }

        @Test
        @TestSecurity(user = "mgr", roles = {"MANAGER"})
        void listDriversShouldRejectInvalidPageSize() {
                given()
                                .queryParam("size", 0)
                                .when().get("/api/drivers")
                                .then()
                                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must be greater than or equal to 1")));
        }

        private Long createDriver(String name, String phone) {
                return given()
                                .contentType(ContentType.JSON)
                                .body(Map.of("name", name, "phone", phone))
                                .when().post("/api/drivers")
                                .then()
                                .statusCode(201)
                                .extract().jsonPath().getLong("id");
        }
}
