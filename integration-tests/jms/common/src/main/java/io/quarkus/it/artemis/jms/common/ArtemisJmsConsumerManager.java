package io.quarkus.it.artemis.jms.common;

import java.util.Optional;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

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
