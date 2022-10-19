package io.quarkus.artemis.test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.io.FileUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ArtemisTestResource implements QuarkusTestResourceLifecycleManager {
    private static final String DEFAULT_CONFIGURATION_NAME = "<default>";

    private final String configurationName;
    private EmbeddedActiveMQ embedded;

    @SuppressWarnings("unused")
    public ArtemisTestResource() {
        this(DEFAULT_CONFIGURATION_NAME);
    }

    protected ArtemisTestResource(String configurationName) {
        this.configurationName = Objects.requireNonNull(configurationName);
    }

    @Override
    public Map<String, String> start() {
        try {
            final String artemisPath = String.format("./target/artemis/%s", configurationName);
            FileUtils.deleteDirectory(Paths.get(artemisPath).toFile());
            embedded = new EmbeddedActiveMQ()
                    .setConfigResourcePath(getConfigurationFileName());
            embedded.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start embedded ActiveMQ server for configuration " + configurationName, e);
        }

        for (TransportConfiguration config : embedded.getConfiguration().getAcceptorConfigurations()) {
            if (config.getName().equals("activemq")) {
                return Collections.singletonMap(
                        getUrlConfigKey(),
                        String.format("tcp://%s:%s", config.getParams().get("host"), config.getParams().get("port")));
            }
        }

        return Collections.emptyMap();
    }

    private String getConfigurationFileName() {
        if (configurationName.equals(DEFAULT_CONFIGURATION_NAME)) {
            return "broker.xml";
        }
        return String.format("broker-%s.xml", configurationName);
    }

    private String getUrlConfigKey() {
        if (configurationName.equals(DEFAULT_CONFIGURATION_NAME)) {
            return "quarkus.artemis.url";
        }
        return String.format("quarkus.artemis.\"%s\".url", configurationName);
    }

    @Override
    public void stop() {
        if (embedded != null) {
            try {
                embedded.stop();
            } catch (Exception e) {
                throw new RuntimeException("Could not stop embedded ActiveMQ server for configuration " + configurationName, e);
            }
        }
    }
}
