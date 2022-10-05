package io.quarkus.it.artemis.jms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public abstract class BaseArtemisHealthCheckTest {
    @Test
    void testHealth() {
        test("/q/health");
    }

    @Test
    void testReady() {
        test("/q/health/ready");
    }

    private static void test(String address) {
        Response response = RestAssured.with().get(address);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());

        Map<String, Object> body = response.as(new TypeRef<>() {
        });
        Assertions.assertEquals("UP", body.get("status"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) body.get("checks");
        Assertions.assertEquals(1, checks.size());
        Map<String, Object> check = checks.get(0);
        Assertions.assertEquals("Artemis JMS health check", check.get("name"));

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) check.get("data");
        Assertions.assertEquals(2, data.size());
        Set<String> namedConnections = Set.of("<default>", "named-1");
        Assertions.assertEquals(namedConnections, data.keySet());
        for (String namedConnection : namedConnections) {
            Assertions.assertEquals("UP", data.get(namedConnection));
        }
    }
}
