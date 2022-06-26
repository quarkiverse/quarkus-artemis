package io.quarkus.it.artemis;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;

import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import io.restassured.response.Response;

abstract public class BaseArtemisProducerTest implements ArtemisHelper {

    public void test() throws Exception {
        String body = createBody();
        Response response = RestAssured.with().body(body).post("/artemis");
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext context = createContext()) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("test-jms"));
            Message message = consumer.receive(1000L);
            Assertions.assertEquals(body, message.getBody(String.class));
        }
    }
}
