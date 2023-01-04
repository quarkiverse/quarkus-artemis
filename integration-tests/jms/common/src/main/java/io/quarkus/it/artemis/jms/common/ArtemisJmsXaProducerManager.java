package io.quarkus.it.artemis.jms.common;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

public class ArtemisJmsXaProducerManager extends ArtemisJmsProducerManager {

    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionManager tm;

    /**
     * This constructor exists solely for CDI ("You need to manually add a non-private no-args constructor").
     */
    @SuppressWarnings("unused")
    ArtemisJmsXaProducerManager() {
        this(null, null, null, null);
    }

    public ArtemisJmsXaProducerManager(
            ConnectionFactory connectionFactory,
            XAConnectionFactory xaConnectionFactory,
            TransactionManager tm,
            String queueName) {
        super(connectionFactory, queueName);
        this.xaConnectionFactory = xaConnectionFactory;
        this.tm = tm;
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
