package io.quarkus.it.artemis.ra;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.transaction.Transactional;

import io.quarkiverse.ironjacamar.ResourceEndpoint;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;

@ApplicationScoped
@ResourceEndpoint(activationSpecConfigKey = "sales")
public class SalesEndpoint implements MessageListener {

    @Inject
    ConnectionFactory connectionFactory;

    AtomicInteger count = new AtomicInteger(0);

    @Override
    @Transactional
    public void onMessage(Message message) {
        try {
            Log.info("####### QuarkusTransaction.isActive = " + QuarkusTransaction.isActive());
            String body = message.getBody(String.class);
            Log.infof("######### Received message from Sales queue: %s", body);
            Log.info("######### Redelivered: " + message.getJMSRedelivered());
            Log.infof("######### Delivered %d times", message.getIntProperty("JMSXDeliveryCount"));
            try (JMSContext context = connectionFactory.createContext()) {
                context.createProducer().send(message.getJMSReplyTo(), "Replied: " + body);
            }
            count.incrementAndGet();
            QuarkusTransaction.setRollbackOnly();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return count.get();
    }
}
