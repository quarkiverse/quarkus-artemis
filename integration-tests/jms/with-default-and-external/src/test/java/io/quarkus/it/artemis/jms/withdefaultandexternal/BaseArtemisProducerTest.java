package io.quarkus.it.artemis.jms.withdefaultandexternal;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

abstract public class BaseArtemisProducerTest implements ArtemisHelper {
    @Test
    void testDefault() throws Exception {
        test(createDefaultContext(), "test-jms-default", "/artemis");
    }

    @Test
    void testNamedOne() throws Exception {
        test(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1");
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
