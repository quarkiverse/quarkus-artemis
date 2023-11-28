package io.quarkus.it.artemis.ra;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MetricsTest {

    @Test
    void shouldHaveMetrics() {
        given()
                .when().get("/q/metrics")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("ironjacamar_pool_in_use_count_total{resourceAdapter=\"<default>\"}"));
    }
}
