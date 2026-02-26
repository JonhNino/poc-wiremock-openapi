package org.acme.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@ConnectWireMock
class CustomerResourceTest {

    WireMock wiremock;

    @BeforeEach
    void resetStubs() {
        wiremock.resetMappings();
    }

    // ── Stub 1: respuesta exitosa 200 con lista de clientes ──────────────────
    @Test
    void testGetCustomers_success_200() {
        wiremock.register(
                get(urlEqualTo("/wiremock"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        [
                                          {
                                            "id": "C001",
                                            "name": "Ana Martinez",
                                            "age": 28,
                                            "status": "ACTIVE",
                                            "email": "ana.martinez@email.com"
                                          },
                                          {
                                            "id": "C002",
                                            "name": "Carlos Ramirez",
                                            "age": 35,
                                            "status": "INACTIVE",
                                            "email": "carlos.ramirez@email.com"
                                          },
                                          {
                                            "id": "C003",
                                            "name": "Laura Gómez",
                                            "age": 42,
                                            "status": "PENDING",
                                            "email": "laura.gomez@email.com"
                                          }
                                        ]
                                        """))
        );

        given()
                .header("Authorization", "Bearer test-token")
                .header("session-id", "sess-001")
                .header("origin-device", "mobile")
        .when()
                .get("/customers")
        .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].id",     is("C001"))
                .body("[0].name",   is("Ana Martinez"))
                .body("[0].age",    is(28))
                .body("[0].status", is("ACTIVE"))
                .body("[0].email",  is("ana.martinez@email.com"))
                .body("[1].id",     is("C002"))
                .body("[1].name",   is("Carlos Ramirez"))
                .body("[1].status", is("INACTIVE"))
                .body("[2].id",     is("C003"))
                .body("[2].name",   is("Laura Gómez"))
                .body("[2].status", is("PENDING"));
    }

    // ── Stub 2: respuesta de error 401 con estructura ErrorResponse ──────────
    @Test
    void testGetCustomers_error_401() {
        wiremock.register(
                get(urlEqualTo("/wiremock"))
                        .willReturn(aResponse()
                                .withStatus(401)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                          "code": "401",
                                          "description": "Unauthorized access - invalid or expired token"
                                        }
                                        """))
        );

        given()
                .header("Authorization", "Bearer token-invalido")
                .header("session-id", "sess-002")
                .header("origin-device", "web")
        .when()
                .get("/customers")
        .then()
                .statusCode(401)
                .body("code",        is("401"))
                .body("description", is("Unauthorized access - invalid or expired token"));
    }
}

