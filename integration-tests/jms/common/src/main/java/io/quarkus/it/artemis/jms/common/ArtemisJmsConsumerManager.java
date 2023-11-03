package io.quarkus.it.artemis.jms.common;

import java.util.Optional;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

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
            Optional<Message> maybeMessage = Optional.ofNullable(consumer.receive(1000L));
            if (maybeMessage.isPresent()) {
                return maybeMessage.get().getBody(String.class);
            } else {
                return null;
            }
        } catch (JMSException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
