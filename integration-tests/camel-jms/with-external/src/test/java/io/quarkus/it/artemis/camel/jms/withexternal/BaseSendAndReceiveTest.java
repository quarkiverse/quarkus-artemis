package io.quarkus.it.artemis.camel.jms.withexternal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;

@TestHTTPEndpoint(ArtemisEndpoint.class)
public abstract class BaseSendAndReceiveTest {
    @Test

    void test() {
        String body = "body";

        given()
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(is(Response.Status.OK.getStatusCode()))
                .body(is(body));
    }
}
