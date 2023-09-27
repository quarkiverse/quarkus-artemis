package io.quarkus.it.artemis.jms.common;

import java.util.Random;

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ArtemisJmsHelper {
    private static final Random RANDOM = new Random();

    public String createBody() {
        return Integer.toString(RANDOM.nextInt(Integer.MAX_VALUE), 16);
    }

    public JMSContext createDefaultContext() {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.url", String.class);
        return new ActiveMQJMSConnectionFactory(url).createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }

    public JMSContext createNamedOneContext() {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.\"named-1\".url", String.class);
        return new ActiveMQJMSConnectionFactory(url).createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }

    public JMSContext createExternallyDefinedContext(String urlConfigName) {
        String url = ConfigProvider.getConfig().getValue(urlConfigName, String.class);
        return new ActiveMQJMSConnectionFactory(url).createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }

    public void sendAndVerify(JMSContext context, String queueName, String endpoint) {
        String body = createBody();
        try (JMSContext ignored = context) {
            context.createProducer().send(context.createQueue(queueName), body);
        }

        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());
    }

    public void receiveAndVerify(String endpoint, JMSContext context, String queueName) throws JMSException {
        String body = createBody();
        Response response = RestAssured.with().body(body).post(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertEquals(body, message.getBody(String.class));
        }
    }

    public void testRollback(String endpoint, JMSContext context, String queueName) {
        Response response = RestAssured.with().body("fail").post(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertNull(message);
        }
    }

    public void sendAndVerifyXACommit(JMSContext context, String queueName, String xaEndpoint, String endpoint) {
        String body = createBody();
        try (JMSContext autoClosedContext = context) {
            context.createProducer().send(context.createQueue(queueName), body);
        }

        // Consume the message in xa transaction
        Response response = RestAssured.with().get(xaEndpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());

        // Receive from queue again to confirm nothing is received i.e. message was consumed
        // and now there is no message in the queue
        response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.statusCode());
    }

    public void sendAndVerifyXARollback(JMSContext context, String queueName, String xaEndpoint, String endpoint) {
        String body = createBody();
        try (JMSContext autoClosedContext = context) {
            context.createProducer().send(context.createQueue(queueName), body);
        }

        // Consume the message but in rollback transaction
        Response response = RestAssured.with().get(xaEndpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());

        // Receive from queue again to confirm message is received i.e. message wasn't consumed
        // and rollback occurred
        response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(),
                response.statusCode());
    }
}
