package io.quarkus.it.artemis.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public class ArtemisHealthCheckHelper {

    public static void testCore(String endpoint, Set<String> expectedConfigurations) {
        test(endpoint, expectedConfigurations, "Artemis Core health check");
    }

    public static void testJms(String endpoint, Set<String> expectedConfigurations) {
        test(endpoint, expectedConfigurations, "Artemis JMS health check");
    }

    private static void test(String endpoint, Set<String> expectedConfigurations, String healthCheckMessage) {
        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());

        Map<String, Object> body = response.as(new TypeRef<>() {
        });
        Assertions.assertEquals("UP", body.get("status"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) body.get("checks");
        Assertions.assertEquals(1, checks.size());
        Map<String, Object> check = checks.get(0);
        Assertions.assertEquals(healthCheckMessage, check.get("name"));

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) check.getOrDefault("data", new HashMap<>());
        Assertions.assertEquals(expectedConfigurations.size(), data.size());
        Assertions.assertEquals(expectedConfigurations, data.keySet());
        for (String namedConfiguration : expectedConfigurations) {
            Assertions.assertEquals("UP", data.get(namedConfiguration));
        }
    }

    private ArtemisHealthCheckHelper() {
    }
}
