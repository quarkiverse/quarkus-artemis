package io.quarkus.it.artemis.jms.withdefault.changeurl;

import jakarta.jms.JMSContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisConsumerTest implements ArtemisHelper {
    @Test
    void testDefault() {
        test(createDefaultContext(), "test-jms-default", "/artemis");
    }

    @Test
    void testNamedOne() {
        test(createNamedOnContext(), "test-jms-named-1", "/artemis/named-1");
    }

    private void test(JMSContext context, String queueName, String endpoint) {
        String body = createBody();
        try (JMSContext ignored = context) {
            context.createProducer().send(context.createQueue(queueName), body);
        }

        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());
    }
}
