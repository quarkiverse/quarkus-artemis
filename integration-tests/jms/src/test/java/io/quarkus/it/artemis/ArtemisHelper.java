package io.quarkus.it.artemis;

import java.util.Random;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public interface ArtemisHelper {

    default String createBody() {
        return Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 16);
    }

    default JMSContext createContext() throws JMSException {
        return new ActiveMQJMSConnectionFactory("tcp://localhost:61616").createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }
}
