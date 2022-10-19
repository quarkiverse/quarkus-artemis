package io.quarkus.it.artemis.jms.empty;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public abstract class BaseArtemisHealthCheckTest {
    @Test
    void testHealth() {
        testUpAndEmpty("/q/health");
    }

    @Test
    void testReady() {
        testUpAndEmpty("/q/health/ready");
    }

    private static void testUpAndEmpty(String endpoint) {
        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());

        Map<String, Object> body = response.as(new TypeRef<>() {
        });
        Assertions.assertEquals("UP", body.get("status"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) body.get("checks");
        Assertions.assertEquals(0, checks.size());
    }
}
