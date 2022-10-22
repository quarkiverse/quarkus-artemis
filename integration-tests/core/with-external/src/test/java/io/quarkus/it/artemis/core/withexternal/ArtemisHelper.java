package io.quarkus.it.artemis.core.withexternal;

import java.util.Random;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.eclipse.microprofile.config.ConfigProvider;

public interface ArtemisHelper {
    default String createBody() {
        return Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 16);
    }

    default ClientSession createExternallyDefinedSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("artemis.externally-defined.url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }
}
