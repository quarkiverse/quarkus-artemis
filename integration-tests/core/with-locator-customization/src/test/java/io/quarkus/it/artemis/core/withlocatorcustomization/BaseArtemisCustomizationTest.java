package io.quarkus.it.artemis.core.withlocatorcustomization;

import jakarta.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;
import io.restassured.RestAssured;

public abstract class BaseArtemisCustomizationTest extends ArtemisCoreHelper {

    @Test
    void testDefaultConfig() {
        // @formatter:off
        RestAssured
            .when().get("/artemis/default/consumer-window-size")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("2048"));
        RestAssured
            .when().get("/artemis/default/call-timeout")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("5000"));
        RestAssured
            .when().get("/artemis/default/auto-group")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("true"));
        // @formatter:on
    }

    @Test
    void testNamedOneConfig() {
        // @formatter:off
        RestAssured
            .when().get("/artemis/named-1/producer-max-rate")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("100"));
        RestAssured
            .when().get("/artemis/named-1/retry-interval-multiplier")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("2.5"));
        RestAssured
            .when().get("/artemis/named-1/pre-acknowledge")
            .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("true"));
        // @formatter:on
    }
}
