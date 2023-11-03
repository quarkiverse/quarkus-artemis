package io.quarkus.it.artemis.core.common;

import java.util.Optional;

import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
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
                Optional<ClientMessage> maybeMessage = Optional.ofNullable(consumer.receive(1000L));
                if (maybeMessage.isPresent()) {
                    maybeMessage.get().acknowledge();
                }
                return maybeMessage.map(ClientMessage::getBodyBuffer)
                        .map(ActiveMQBuffer::readString)
                        .orElse(null);
            }
        } catch (ActiveMQException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
