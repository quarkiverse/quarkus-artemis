package io.quarkus.it.artemis.camel.jms.withexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        ArtemisProducerManager namedProducerManager(
                @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory) {
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
