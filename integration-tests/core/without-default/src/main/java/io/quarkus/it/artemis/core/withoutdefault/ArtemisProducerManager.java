package io.quarkus.it.artemis.core.withoutdefault;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.*;

import io.smallrye.common.annotation.Identifier;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        @Identifier("named-1")
        public static ArtemisProducerManager namedOneProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneServerLocator)
                throws Exception {
            return new ArtemisProducerManager(namedOneServerLocator, "test-core-named-1");
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
