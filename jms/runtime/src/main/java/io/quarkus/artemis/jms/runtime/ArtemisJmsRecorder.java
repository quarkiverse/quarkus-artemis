package io.quarkus.artemis.jms.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.artemis.core.runtime.*;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {
    private HashMap<String, ConnectionFactoryConfig> connectionFactoryConfigs;

    public void init(
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            Collection<String> connectionNames) {
        connectionFactoryConfigs = new HashMap<>();
        for (String name : connectionNames) {
            connectionFactoryConfigs.put(
                    name,
                    new ConnectionFactoryConfig(
                            name,
                            runtimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig()),
                            buildTimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisBuildTimeConfig())));
        }
    }

    public ArtemisJmsWrapper getDefaultWrapper() {
        return new ArtemisJmsWrapper() {
            @Override
            public ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory cf) {
                return cf;
            }
        };
    }

    public Supplier<ConnectionFactory> getConnectionFactoryProducer(
            String name,
            ArtemisJmsWrapper wrapper) {
        ConnectionFactoryConfig config = connectionFactoryConfigs.get(name);
        ConnectionFactory connectionFactory;
        connectionFactory = extractConnectionFactory(wrapper, config);
        return new Supplier<>() {
            @Override
            public ConnectionFactory get() {
                return connectionFactory;
            }
        };
    }

    private static ConnectionFactory extractConnectionFactory(ArtemisJmsWrapper wrapper, ConnectionFactoryConfig config) {
        if (config.getUrl() != null) {
            return wrapper.wrapConnectionFactory(new ActiveMQConnectionFactory(
                    config.getUrl(),
                    config.getUsername(),
                    config.getPassword()));
        }
        return null;
    }

    private static class ConnectionFactoryConfig {
        final String url;
        final ArtemisRuntimeConfig runtimeConfig;

        private ConnectionFactoryConfig(
                String name,
                ArtemisRuntimeConfig runtimeConfig,
                ArtemisBuildTimeConfig buildTimeConfig) {
            this.runtimeConfig = runtimeConfig;
            if (buildTimeConfig == null) {
                buildTimeConfig = new ArtemisBuildTimeConfig();
            }
            ArtemisUtil.validateIntegrity(runtimeConfig, buildTimeConfig, name);
            this.url = runtimeConfig.getUrl();
        }

        public String getUrl() {
            return runtimeConfig.getUrl();
        }

        public String getUsername() {
            return runtimeConfig.getUsername();
        }

        public String getPassword() {
            return runtimeConfig.getPassword();
        }
    }
}
