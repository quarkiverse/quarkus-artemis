package io.quarkus.it.artemis;

import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisProducerTest implements ArtemisHelper {

    public void test() throws Exception {
        String body = createBody();
        Response response = RestAssured.with().body(body).post("/artemis");
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (ClientSession session = createSession()) {
            session.start();
            ClientMessage message = session.createConsumer("test-core").receive(1000L);
            message.acknowledge();
            Assertions.assertEquals(body, message.getBodyBuffer().readString());
        }
    }
}
