package io.quarkus.it.artemis.jms.withexternal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

import io.smallrye.common.annotation.Identifier;

public class ArtemisConsumerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        @Identifier("externally-defined")
        ArtemisConsumerManager externallyDefinedManager(
                @Identifier("externally-defined") ConnectionFactory namedOneConnectionFactory) {
            return new ArtemisConsumerManager(namedOneConnectionFactory, "test-jms-externally-defined");
        }

        Producer() {
        }
    }

    private final ConnectionFactory connectionFactory;
    private final String queueName;

    public ArtemisConsumerManager(ConnectionFactory connectionFactory, String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    public String receive() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createQueue(queueName))) {
            return consumer.receive(1000L).getBody(String.class);
        } catch (JMSException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
