package io.quarkus.it.artemis.ra;

import static org.hamcrest.Matchers.is;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
class JcaResourceTest {
    @BeforeEach
    void setup() {
        // @formatter:off
        RestAssured
                .when().delete("/jca/gifts")
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
        RestAssured
                .when().put("/myqueue/reset")
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
        // @formatter:on
    }

    @Test
    void testProducer() {
        // @formatter:off
        RestAssured
                .when().get("/jca?name=George")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("Hello George"));
        RestAssured
                .when().get("/jca/gifts/count")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("1"));
        RestAssured
                .when().get("/myqueue")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("1"));
        // @formatter:on
    }

    @Test
    void testProducerRollback() {
        // @formatter:off
        RestAssured
                .when().get("/jca?name=rollback")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("Hello rollback"));
        RestAssured
                .when().get("/jca/gifts/count")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("0"));
        RestAssured
                .when().get("/myqueue")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("0"));
        // @formatter:on
    }

    @Test
    void testTransacted() {
        // @formatter:off
        RestAssured
                .when().get("/jca/transacted")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("true"));
        // @formatter:on
    }

    @Test
    void testNotTransacted() {
        // @formatter:off
        RestAssured
                .when().get("/jca/not-transacted")
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is("false"));
        // @formatter:on
    }
}
