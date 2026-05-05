package org.acme.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthJwtIntegrationTest {

    @Test
    void loginShouldReturnSignedJwtForRegisteredUser() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "JWT Integration User",
                        "email", email,
                        "password", "Secret@123"
                ))
                .when().post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("message", equalTo("User registered"))
                .body("userId", notNullValue());

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "Secret@123"
                ))
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$"))
                .body("email", equalTo(email))
                .body("role", equalTo("MANAGER"));
    }

    @Test
    void loginShouldRejectWrongPasswordForRegisteredUser() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "JWT Invalid Password User",
                        "email", email,
                        "password", "Secret@123"
                ))
                .when().post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("message", equalTo("User registered"))
                .body("userId", notNullValue());

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "WrongPass"
                ))
                .when().post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("Invalid credentials"))
                .body("status", equalTo(401))
                .body("path", containsString("/api/auth/login"));
    }

    @Test
    void adminTokenShouldListUsers() {
        String adminEmail = uniqueEmail();
        String managerEmail = uniqueEmail();

        registerUser("Admin Integration User", adminEmail, "Secret@123", "ADMIN");
        registerUser("Manager Integration User", managerEmail, "Secret@123", "MANAGER");

        String adminToken = loginAndGetToken(adminEmail, "Secret@123");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when().get("/api/auth/users?page=0&size=20")
                .then()
                .statusCode(200)
                .body("email", hasItem(adminEmail))
                .body("email", hasItem(managerEmail));
    }

    @Test
    void adminTokenShouldDeactivateUserAndBlockFutureLogin() {
        String adminEmail = uniqueEmail();
        String managerEmail = uniqueEmail();

        registerUser("Admin Deactivate User", adminEmail, "Secret@123", "ADMIN");
        Long managerUserId = registerUser("Manager To Deactivate", managerEmail, "Secret@123", "MANAGER");

        String adminToken = loginAndGetToken(adminEmail, "Secret@123");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when().put("/api/auth/users/{id}/deactivate", managerUserId)
                .then()
                .statusCode(200)
                .body("message", equalTo("User deactivated"));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", managerEmail,
                        "password", "Secret@123"
                ))
                .when().post("/api/auth/login")
                .then()
                .statusCode(403)
                .body("error", equalTo("Account is inactive"))
                .body("status", equalTo(403));
    }

    private Long registerUser(String name, String email, String password, String role) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "email", email,
                        "password", password,
                        "role", role
                ))
                .when().post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("message", equalTo("User registered"))
                .body("userId", notNullValue())
                .extract()
                .jsonPath()
                .getLong("userId");
    }

    private String loginAndGetToken(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", password
                ))
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$"))
                .body("email", equalTo(email))
                .extract()
                .jsonPath()
                .getString("token");
    }

    private String uniqueEmail() {
        return "jwt-" + UUID.randomUUID() + "@travel.com";
    }
}