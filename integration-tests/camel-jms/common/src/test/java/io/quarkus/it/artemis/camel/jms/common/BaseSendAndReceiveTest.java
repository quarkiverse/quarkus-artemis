package io.quarkus.it.artemis.camel.jms.common;

import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

public interface BaseSendAndReceiveTest {
    @Test
    default void test() {
        String body = "body";
        RestAssured.given().body(body)
                .when().post()
                .then().statusCode(is(Response.Status.OK.getStatusCode())).body(is(body));
    }
}
