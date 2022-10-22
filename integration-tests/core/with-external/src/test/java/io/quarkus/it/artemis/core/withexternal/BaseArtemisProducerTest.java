package io.quarkus.it.artemis.core.withexternal;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisProducerTest implements ArtemisHelper {
    @Test
    void testExternallyDefined() throws Exception {
        test(createExternallyDefinedSession(), "/artemis/externally-defined", "test-core-externally-defined");
    }

    protected void test(ClientSession session, String endpoint, String queueName) throws ActiveMQException {
        String body = createBody();
        Response response = RestAssured.with().body(body).post(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (ClientSession autoClosedSession = session) {
            session.start();
            ClientMessage message = autoClosedSession.createConsumer(queueName).receive(1000L);
            message.acknowledge();
            Assertions.assertEquals(body, message.getBodyBuffer().readString());
        }
    }
}
