package io.quarkus.it.artemis.core.withexternal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.*;

import io.smallrye.common.annotation.Identifier;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        @Identifier("externally-defined")
        public static ArtemisProducerManager externallyAddedProducer(
                @Identifier("externally-defined") ServerLocator externallyAddedServerLocator)
                throws Exception {
            return new ArtemisProducerManager(externallyAddedServerLocator, "test-core-externally-defined");
        }

        Producer() {
        }
    }

    private final ClientSessionFactory sessionFactory;
    private final String queueName;

    private ArtemisProducerManager(ServerLocator serverLocator, String queueName) throws Exception {
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
        } catch (ActiveMQException e) {
            throw new RuntimeException("Could not send message", e);
        }
    }
}
