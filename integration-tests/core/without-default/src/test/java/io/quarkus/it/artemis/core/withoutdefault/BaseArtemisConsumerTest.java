package io.quarkus.it.artemis.core.withoutdefault;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisConsumerTest implements ArtemisHelper {
    @Test
    void testNamedOne() throws Exception {
        test(createNamedOneSession(), "test-core-named-1", "/artemis/named-1");
    }

    private void test(ClientSession session, String addressName, String endpoint) throws ActiveMQException {
        String body = createBody();
        try (ClientSession autoClosedSession = session) {
            ClientMessage message = autoClosedSession.createMessage(true);
            message.getBodyBuffer().writeString(body);
            autoClosedSession.createProducer(addressName).send(message);
        }

        Response response = RestAssured.with().get(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.OK.getStatusCode(), response.statusCode());
        Assertions.assertEquals(body, response.getBody().asString());
    }
}
