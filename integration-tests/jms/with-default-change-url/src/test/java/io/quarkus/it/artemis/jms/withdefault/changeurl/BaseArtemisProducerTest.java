package io.quarkus.it.artemis.jms.withdefault.changeurl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

abstract public class BaseArtemisProducerTest implements ArtemisHelper {
    @Test
    void testDefault() throws Exception {
        test(createDefaultContext(), "/artemis", "test-jms-default");
    }

    @Test
    void testNamedOne() throws Exception {
        test(createNamedOnContext(), "/artemis/named-1", "test-jms-named-1");
    }

    private void test(JMSContext context, String endpoint, String queueName) throws JMSException {
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
