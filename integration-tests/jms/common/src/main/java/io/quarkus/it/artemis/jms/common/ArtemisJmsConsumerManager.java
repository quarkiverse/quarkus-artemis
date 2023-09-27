package io.quarkus.it.artemis.jms.common;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

public class ArtemisJmsConsumerManager {
    private final ConnectionFactory connectionFactory;
    private final String queueName;

    public ArtemisJmsConsumerManager(ConnectionFactory connectionFactory, String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    public String receive() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createQueue(queueName))) {
            return consumer.receive(1000L).getBody(String.class);
        } catch (JMSException | NullPointerException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
