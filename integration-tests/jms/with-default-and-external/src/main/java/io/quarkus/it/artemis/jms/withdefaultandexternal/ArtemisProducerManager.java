package io.quarkus.it.artemis.jms.withdefaultandexternal;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.*;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

public class ArtemisProducerManager {
    static class Producer {
        @Produces
        @ApplicationScoped
        public ArtemisProducerManager defaultProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") XAConnectionFactory defaultXaConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
            return new ArtemisProducerManager(defaultConnectionFactory, defaultXaConnectionFactory, tm, "test-jms-default");
        }

        @Produces
        @ApplicationScoped
        @Identifier("named-1")
        public ArtemisProducerManager namedOneProducer(
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") XAConnectionFactory namedOneXaConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
            return new ArtemisProducerManager(
                    namedOneConnectionFactory,
                    namedOneXaConnectionFactory,
                    tm,
                    "test-jms-named-1");
        }

        @Produces
        @ApplicationScoped
        @Identifier("externally-defined")
        public ArtemisProducerManager externallyDefinedProducer(
                @Identifier("externally-defined") ConnectionFactory externallyDefinedConnectionFactory,
                @Identifier("externally-defined") XAConnectionFactory externallyDefinedXaConnectionFactory,
                @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
            return new ArtemisProducerManager(
                    externallyDefinedConnectionFactory,
                    externallyDefinedXaConnectionFactory,
                    tm,
                    "test-jms-externally-defined");
        }

        Producer() {
        }
    }

    private final ConnectionFactory connectionFactory;
    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionManager tm;
    private final String queueName;

    public ArtemisProducerManager(
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
