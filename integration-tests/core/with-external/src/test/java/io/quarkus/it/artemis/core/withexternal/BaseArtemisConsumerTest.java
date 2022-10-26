package io.quarkus.it.artemis.core.withexternal;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisConsumerTest implements ArtemisHelper {
    @Test
    void testExternallyDefined() throws Exception {
        test(createExternallyDefinedSession(), "test-core-externally-defined", "/artemis/externally-defined");
    }

    protected void test(ClientSession session, String addressName, String endpoint) throws ActiveMQException {
        String body = createBody();
        try (ClientSession autoClosedSession = session) {
            ClientMessage message = autoClosedSession.createMessage(true);
            message.getBodyBuffer().writeString(body);
            autoClosedSession.createProducer(addressName).send(message);
        }

        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(jakarta.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());
    }
}
