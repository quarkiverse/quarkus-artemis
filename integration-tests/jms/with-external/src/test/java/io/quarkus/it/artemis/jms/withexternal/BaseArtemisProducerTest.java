package io.quarkus.it.artemis.jms.withexternal;

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

abstract public class BaseArtemisProducerTest implements ArtemisHelper {
    @Test
    void testExternallyDefined() throws Exception {
        test(createExternallyDefinedContext(), "test-jms-externally-defined", "/artemis/externally-defined");
    }

    protected void test(JMSContext context, String queueName, String endpoint) throws JMSException {
        String body = createBody();
        Response response = RestAssured.with().body(body).post(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertEquals(body, message.getBody(String.class));
        }
    }
}
