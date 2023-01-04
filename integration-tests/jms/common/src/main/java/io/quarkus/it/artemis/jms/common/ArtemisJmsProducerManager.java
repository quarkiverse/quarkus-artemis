package io.quarkus.it.artemis.jms.common;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;

public class ArtemisJmsProducerManager {
    private final ConnectionFactory connectionFactory;
    private final String queueName;

    public ArtemisJmsProducerManager(ConnectionFactory connectionFactory, String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    public void send(String body) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            send(context, body);
        }
    }

    protected void send(JMSContext context, String body) {
        JMSProducer producer = context.createProducer();
        producer.send(context.createQueue(queueName), body);
    }
}