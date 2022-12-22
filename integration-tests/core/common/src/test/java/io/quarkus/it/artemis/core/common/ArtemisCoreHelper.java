package io.quarkus.it.artemis.core.common;

import java.util.Random;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ArtemisCoreHelper {
    private static final Random RANDOM = new Random();

    public String createBody() {
        return Integer.toString(RANDOM.nextInt(Integer.MAX_VALUE), 16);
    }

    public ClientSession createDefaultSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }

    public ClientSession createNamedOneSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.\"named-1\".url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }

    public void sendAndVerify(ClientSession session, String addressName, String endpoint) throws ActiveMQException {
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

    public void receiveAndVerify(String endpoint, ClientSession session, String queueName) throws ActiveMQException {
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

    public ClientSession createExternallyDefinedSession(String urlConfigName) throws Exception {
        String url = ConfigProvider.getConfig().getValue(urlConfigName, String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }
}
