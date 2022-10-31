package io.quarkus.it.artemis.jms.withdefault;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class ArtemisConsumerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        ArtemisConsumerManager defaultConsumerManager(
                @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory) {
            return new ArtemisConsumerManager(connectionFactory, "test-jms-default");
        }

        @Produces
        @ApplicationScoped
        @Identifier("named-1")
        ArtemisConsumerManager namedOneConsumerManager(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory) {
            return new ArtemisConsumerManager(namedOneConnectionFactory, "test-jms-named-1");
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
