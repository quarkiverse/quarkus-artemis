package io.quarkus.it.artemis.jms.common;

import static org.hamcrest.Matchers.is;

import java.util.Random;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.core.Response;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;

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

        // @formatter:off
        RestAssured
                .when().get(endpoint)
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is(body));
        // @formatter:on
    }

    public void receiveAndVerify(String endpoint, JMSContext context, String queueName) throws JMSException {
        String body = createBody();
        RestAssured
                .given().body(body)
                .when().post(endpoint)
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertEquals(body, message.getBody(String.class));
        }
    }

    public void testRollback(String endpoint, JMSContext context, String queueName) {
        RestAssured
                .given().body("fail")
                .when().post(endpoint)
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertNull(message);
        }
    }

    public void sendAndVerifyXACommit(JMSContext context, String queueName, String xaEndpoint, String endpoint) {
        String body = createBody();
        try (JMSContext autoClosedContext = context) {
            autoClosedContext.createProducer().send(context.createQueue(queueName), body);
        }

        // Consume the message in xa transaction
        // @formatter:off
        RestAssured
                .when().get(xaEndpoint)
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is(body));
        // @formatter:on
        // Receive from queue again to confirm nothing is received i.e. message was consumed
        // and now there is no message in the queue
        RestAssured
                .when().get(endpoint)
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    public void sendAndVerifyXARollback(JMSContext context, String queueName, String xaEndpoint, String endpoint) {
        String body = createBody();
        try (JMSContext autoClosedContext = context) {
            autoClosedContext.createProducer().send(context.createQueue(queueName), body);
        }

        // Consume the message but in rollback transaction
        // @formatter:off
        RestAssured
                .when().get(xaEndpoint)
                .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body(is(body));
        // @formatter:on
        // Receive from queue again to confirm message is received i.e. message wasn't consumed
        // and rollback occurred
        RestAssured
                .when().get(endpoint)
                .then().statusCode(Response.Status.OK.getStatusCode());
    }
}
