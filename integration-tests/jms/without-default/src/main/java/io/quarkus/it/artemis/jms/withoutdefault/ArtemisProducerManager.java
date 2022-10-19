package io.quarkus.it.artemis.jms.withoutdefault;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.jms.*;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import io.smallrye.common.annotation.Identifier;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        @Identifier("named-1")
        ArtemisProducerManager namedOneProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") XAConnectionFactory namedOneXaConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
            return new ArtemisProducerManager(
                    namedOneConnectionFactory,
                    namedOneXaConnectionFactory,
                    tm,
                    "test-jms-named-1");
        }

        Producer() {
        }
    }

    private final ConnectionFactory connectionFactory;
    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionManager tm;
    private final String queueName;

    private ArtemisProducerManager(
            ConnectionFactory connectionFactory,
            XAConnectionFactory xaConnectionFactory,
            TransactionManager tm,
            String queueName) {
        this.connectionFactory = connectionFactory;
        this.xaConnectionFactory = xaConnectionFactory;
        this.tm = tm;
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

    public void sendXA(String body) throws SystemException, RollbackException {
        XAJMSContext context = xaConnectionFactory.createXAContext();
        tm.getTransaction().enlistResource(context.getXAResource());
        tm.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int i) {
                context.close();
            }
        });
        send(context, body);
        if (body.equals("fail")) {
            tm.setRollbackOnly();
        }
    }
}
