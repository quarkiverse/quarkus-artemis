package io.quarkus.it.artemis;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

@ApplicationScoped
public class ArtemisProducerManager {

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    XAConnectionFactory xaConnectionFactory;

    @Inject
    TransactionManager tm;

    private void _send(JMSContext context, String body) {
        JMSProducer producer = context.createProducer();
        producer.send(context.createQueue("test-jms"), body);
    }

    public void send(String body) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            _send(context, body);
        }
    }

    public void sendXA(String body) throws Exception {
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
        _send(context, body);
        if (body.equals("fail")) {
            tm.setRollbackOnly();
        }
    }
}
