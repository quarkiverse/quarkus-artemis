package io.quarkus.it.artemis.core.common;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public class ArtemisCoreConsumerManager {
    private final ClientSessionFactory sessionFactory;
    private final String queueName;

    public ArtemisCoreConsumerManager(ServerLocator serverLocator, String queueName) throws Exception {
        sessionFactory = serverLocator.createSessionFactory();
        this.queueName = queueName;
    }

    public String receive() {
        try (ClientSession session = sessionFactory.createSession()) {
            session.start();
            try (ClientConsumer consumer = session.createConsumer(queueName)) {
                ClientMessage message = consumer.receive(1000L);
                message.acknowledge();
                return message.getBodyBuffer().readString();
            }
        } catch (ActiveMQException | NullPointerException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
