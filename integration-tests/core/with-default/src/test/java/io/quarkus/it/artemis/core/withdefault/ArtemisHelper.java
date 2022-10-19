package io.quarkus.it.artemis.core.withdefault;

import java.util.Random;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.eclipse.microprofile.config.ConfigProvider;

public interface ArtemisHelper {

    default String createBody() {
        return Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 16);
    }

    default ClientSession createDefaultSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }

    default ClientSession createNamedOneSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.\"named-1\".url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }
}
