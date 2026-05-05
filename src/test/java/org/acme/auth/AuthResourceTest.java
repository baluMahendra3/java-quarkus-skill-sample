package org.acme.auth;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

        @InjectMock
        JwtService jwtService;

    @Test
    void registerShouldCreateUser() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "JUnit User",
                        "email", email,
                        "password", "Secret@123"
                ))
                .when().post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("message", equalTo("User registered"))
                .body("userId", notNullValue());
    }

    @Test
    void registerShouldReturnBadRequestWhenRequiredFieldsMissing() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", uniqueEmail()))
                .when().post("/api/auth/register")
                .then()
                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must not be blank")))
                .body("status", equalTo(400));
    }

        @Test
        void loginShouldRejectInvalidPayload() {
                given()
                                .contentType(ContentType.JSON)
                                .body(Map.of("email", "not-an-email"))
                                .when().post("/api/auth/login")
                                .then()
                                .statusCode(400)
                                .body("error", equalTo("Validation failed"))
                                .body("details", hasItem(containsString("must be a well-formed email address")));
        }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        String email = uniqueEmail();
        registerUser("JUnit Login", email, "Secret@123", null);
        Mockito.when(jwtService.generateToken(Mockito.anyLong(), Mockito.eq(email), Mockito.any()))
                .thenReturn("mock-token");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "Secret@123"))
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", equalTo("mock-token"))
                .body("email", equalTo(email))
                .body("role", equalTo("MANAGER"));
    }

    @Test
    void loginShouldReturnUnauthorizedForInvalidPassword() {
        String email = uniqueEmail();
        registerUser("JUnit Invalid", email, "Secret@123", null);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "WrongPass"))
                .when().post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("Invalid credentials"))
                .body("status", equalTo(401));
    }

    @Test
    void listUsersShouldRequireAuthentication() {
        given()
                .when().get("/api/auth/users")
                .then()
                .statusCode(401);
    }

    @Test
        @TestSecurity(user = "admin-sub", roles = {"ADMIN"})
    void listUsersShouldAllowAdminToken() {
        given()
                .when().get("/api/auth/users")
                .then()
                                .statusCode(200);
    }

    private void registerUser(String name, String email, String password, String role) {
        Map<String, Object> requestBody = role == null
                ? Map.of("name", name, "email", email, "password", password)
                : Map.of("name", name, "email", email, "password", password, "role", role);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("userId", notNullValue());
    }

    private String uniqueEmail() {
        return "junit-" + UUID.randomUUID() + "@travel.com";
    }
}