package io.quarkus.it.artemis.camel.jms.withnamedandexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;

import io.smallrye.common.annotation.Identifier;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        @Identifier("named")
        ArtemisProducerManager namedProducerManager(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named") ConnectionFactory defaultConnectionFactory) {
            return new ArtemisProducerManager(defaultConnectionFactory, "in");
        }

        Producer() {
        }
    }

    private final ConnectionFactory connectionFactory;
    private final String queueName;

    private ArtemisProducerManager(
            ConnectionFactory connectionFactory,
            String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    private void send(JMSContext context, String body) {
        JMSProducer producer = context.createProducer();
        producer.send(context.createQueue(queueName), body);
    }

    public void send(String body) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            send(context, body);
        }
    }
}
