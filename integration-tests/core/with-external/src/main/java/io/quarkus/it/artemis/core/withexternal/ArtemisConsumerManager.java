package io.quarkus.it.artemis.core.withexternal;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.*;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class ArtemisConsumerManager {
    static class Producer {

        @Produces
        @ApplicationScoped
        @Identifier("externally-defined")
        ArtemisConsumerManager externallyDefinedConsumer(
                @Identifier("externally-defined") ServerLocator externallyAddedServerLocator) throws Exception {
            return new ArtemisConsumerManager(externallyAddedServerLocator, "test-core-externally-defined");
        }

        Producer() {
        }
    }

    private final ClientSessionFactory sessionFactory;
    private final String queueName;

    private ArtemisConsumerManager(ServerLocator serverLocator, String queueName) throws Exception {
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
        } catch (ActiveMQException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
