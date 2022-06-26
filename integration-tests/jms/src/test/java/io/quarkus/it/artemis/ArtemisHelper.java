package io.quarkus.it.artemis;

import java.util.Random;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;

public interface ArtemisHelper {

    default String createBody() {
        return Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 16);
    }

    default JMSContext createContext() throws JMSException {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.url", String.class);
        return new ActiveMQJMSConnectionFactory(url).createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }
}
