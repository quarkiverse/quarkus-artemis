package io.quarkus.artemis.test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.io.FileUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ArtemisTestResource implements QuarkusTestResourceLifecycleManager {

    private EmbeddedActiveMQ embedded;

    @Override
    public Map<String, String> start() {
        try {
            FileUtils.deleteDirectory(Paths.get("./target/artemis").toFile());
            embedded = new EmbeddedActiveMQ();
            embedded.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start embedded ActiveMQ server", e);
        }

        for (TransportConfiguration config : embedded.getConfiguration().getAcceptorConfigurations()) {
            if (config.getName().equals("activemq")) {
                return Collections.singletonMap("quarkus.artemis.url",
                        String.format("tcp://%s:%s", config.getParams().get("host"), config.getParams().get("port")));
            }
        }

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (embedded == null) {
            return;
        }
        try {
            embedded.stop();
        } catch (Exception e) {
            throw new RuntimeException("Could not stop embedded ActiveMQ server", e);
        }
    }
}
