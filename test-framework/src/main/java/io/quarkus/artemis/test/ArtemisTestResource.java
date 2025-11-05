package io.quarkus.artemis.test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.io.FileUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ArtemisTestResource implements QuarkusTestResourceLifecycleManager {
    private static final String DEFAULT_CONFIGURATION_NAME = "<default>";
    private static final String QUARKUS_ARTEMIS_CONFIG_PREFIX = "quarkus.artemis";
    private static final String INIT_ARG_CONFIGURATION_NAME = "configurationName";
    private static final String INIT_ARG_CONFIGURATION_PREFIX = "configurationPrefix";

    private String configurationName;
    private String configurationPrefix;
    private EmbeddedActiveMQ embedded;

    @SuppressWarnings("unused")
    public ArtemisTestResource() {
        this(DEFAULT_CONFIGURATION_NAME);
    }

    protected ArtemisTestResource(String configurationName) {
        this(QUARKUS_ARTEMIS_CONFIG_PREFIX, configurationName);
    }

    protected ArtemisTestResource(String configurationPrefix, String configurationName) {
        this.configurationPrefix = configurationPrefix;
        this.configurationName = Objects.requireNonNull(configurationName);
    }

    @Override
    public void init(Map<String, String> initArgs) {
        if (initArgs == null || initArgs.isEmpty()) {
            return;
        }
        if (initArgs.containsKey(INIT_ARG_CONFIGURATION_NAME)) {
            this.configurationName = initArgs.get(INIT_ARG_CONFIGURATION_NAME);
        }
        if (initArgs.containsKey(INIT_ARG_CONFIGURATION_PREFIX)) {
            this.configurationPrefix = initArgs.get(INIT_ARG_CONFIGURATION_PREFIX);
        }
    }

    @Override
    public Map<String, String> start() {
        try {
            var artemisPath = Path.of(".", "target", "artemis", getFileSystemSafeName(configurationName));
            FileUtils.deleteDirectory(artemisPath.toFile());
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
                        String.format("tcp://%s:%s", config.getParams().get("host"), resolvePort(config.getName(), config.getParams().get("port"))));
            }
        }

        return Collections.emptyMap();
    }

    private Object resolvePort(String acceptorName, Object configPort) {
        if (Integer.parseInt(configPort.toString()) == 0) {
            return embedded.getActiveMQServer()
                .getRemotingService()
                .getAcceptor(acceptorName)
                .getActualPort();
        }
        return configPort;
    }

    private String getConfigurationFileName() {
        if (configurationName.equals(DEFAULT_CONFIGURATION_NAME)) {
            return "broker.xml";
        }
        return String.format("broker-%s.xml", getFileSystemSafeName(configurationName));
    }

    private static String getFileSystemSafeName(String name) {
        return name.replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    private String getUrlConfigKey() {
        if (configurationName.equals(DEFAULT_CONFIGURATION_NAME)) {
            return "quarkus.artemis.url";
        }
        if (configurationPrefix.equals(QUARKUS_ARTEMIS_CONFIG_PREFIX)) {
            return String.format("%s.\"%s\".url", configurationPrefix, configurationName);
        }
        return String.format("%s.%s.url", configurationPrefix, configurationName);
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
