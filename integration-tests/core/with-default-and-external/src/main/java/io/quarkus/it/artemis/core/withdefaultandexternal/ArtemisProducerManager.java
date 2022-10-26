package io.quarkus.it.artemis.core.withdefaultandexternal;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.*;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        public static ArtemisProducerManager defaultProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") ServerLocator serverLocator) throws Exception {
            return new ArtemisProducerManager(serverLocator, "test-core-default");
        }

        @Produces
        @ApplicationScoped
        @Identifier("named-1")
        public static ArtemisProducerManager namedOneProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneServerLocator)
                throws Exception {
            return new ArtemisProducerManager(namedOneServerLocator, "test-core-named-1");
        }

        @Produces
        @ApplicationScoped
        @Identifier("externally-defined")
        public static ArtemisProducerManager externallyAddedProducer(
                @Identifier("externally-defined") ServerLocator externallyAddedServerLocator) throws Exception {
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
