package io.quarkus.it.artemis.core.withfactorycustomization;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;
import io.restassured.RestAssured;

public abstract class BaseArtemisCustomizationTest extends ArtemisJmsHelper {

    @Test
    void testDefaultConfig() {
        RestAssured.when()
                .get("/artemis/default/consumer-window-size")
                .then()
                .statusCode(200)
                .body(Matchers.is("2048"));

        RestAssured.when()
                .get("/artemis/default/call-timeout")
                .then()
                .statusCode(200)
                .body(Matchers.is("5000"));

        RestAssured.when()
                .get("/artemis/default/auto-group")
                .then()
                .statusCode(200)
                .body(Matchers.is("true"));
    }

    @Test
    void testNamedOneConfig() {
        RestAssured.when()
                .get("/artemis/named-1/producer-max-rate")
                .then()
                .statusCode(200)
                .body(Matchers.is("100"));

        RestAssured.when()
                .get("/artemis/named-1/retry-interval-multiplier")
                .then()
                .statusCode(200)
                .body(Matchers.is("2.5"));

        RestAssured.when()
                .get("/artemis/named-1/pre-acknowledge")
                .then()
                .statusCode(200)
                .body(Matchers.is("true"));
    }
}
