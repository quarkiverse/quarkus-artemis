package io.quarkus.it.artemis.core.common;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public class ArtemisCoreProducerManager {
    private final ClientSessionFactory sessionFactory;
    private final String queueName;

    public ArtemisCoreProducerManager(ServerLocator serverLocator, String queueName) throws Exception {
        this.sessionFactory = serverLocator.createSessionFactory();
        this.queueName = queueName;
    }

    public void send(String body) {
        try (ClientSession session = sessionFactory.createSession()) {
            ClientMessage message = session.createMessage(true);
            message.getBodyBuffer().writeString(body);
            try (ClientProducer producer = session.createProducer(queueName)) {
                producer.send(message);
            }
        } catch (ActiveMQException | NullPointerException e) {
            throw new RuntimeException("Could not send message", e);
        }
    }
}
