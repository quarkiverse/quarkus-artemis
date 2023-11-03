package io.quarkus.it.artemis.jms.common;

import java.util.Optional;

import javax.jms.*;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

public class ArtemisJmsXaConsumerManager extends ArtemisJmsConsumerManager {
    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionManager tm;
    private final String queueName;

    /**
     * This constructor exists solely for CDI ("You need to manually add a non-private no-args constructor").
     */
    @SuppressWarnings("unused")
    ArtemisJmsXaConsumerManager() {
        this(null, null, null, null);
    }

    public ArtemisJmsXaConsumerManager(ConnectionFactory connectionFactory,
            XAConnectionFactory xaConnectionFactory,
            TransactionManager tm,
            String queueName) {
        super(connectionFactory, queueName);
        this.tm = tm;
        this.queueName = queueName;
        this.xaConnectionFactory = xaConnectionFactory;
    }

    public String receiveXA(boolean rollback) throws SystemException, RollbackException {
        XAJMSContext context = xaConnectionFactory.createXAContext();
        JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));

        tm.getTransaction().enlistResource(context.getXAResource());
        tm.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // Nothing to do
            }

            @Override
            public void afterCompletion(int i) {
                consumer.close();
                context.close();
            }
        });
        if (rollback) {
            tm.setRollbackOnly();
        }
        try {
            Optional<Message> maybeMessage = Optional.ofNullable(consumer.receive(1000L));
            if (maybeMessage.isPresent()) {
                return maybeMessage.get().getBody(String.class);
            } else {
                return null;
            }
        } catch (JMSException | NullPointerException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
