package io.quarkus.it.artemis.jms.withexternal;

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
        test("/q/health", Set.of("externally-defined"));
    }

    @Test
    void testReady() {
        test("/q/health/ready", Set.of("externally-defined"));
    }

    private static void test(String endpoint, Set<String> expectedConfigurations) {
        Response response = RestAssured.with().get(endpoint);
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
        Assertions.assertEquals(expectedConfigurations.size(), data.size());
        Assertions.assertEquals(expectedConfigurations, data.keySet());
        for (String namedConfiguration : expectedConfigurations) {
            Assertions.assertEquals("UP", data.get(namedConfiguration));
        }
    }
}
