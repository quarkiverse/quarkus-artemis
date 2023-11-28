package io.quarkus.it.artemis.ra;

import static org.hamcrest.Matchers.containsString;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.ra.profile.DisableAllServices;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(DisableAllServices.class)
public class MetricsTest {

    @Test
    void shouldHaveMetrics() {
        // @formatter:off
        RestAssured
                .when().get("/q/metrics")
                .then()
                .log().ifValidationFails()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(containsString("ironjacamar_pool_in_use_count_total{resourceAdapter=\"<default>\"}"));
        // @formatter:on
    }
}
