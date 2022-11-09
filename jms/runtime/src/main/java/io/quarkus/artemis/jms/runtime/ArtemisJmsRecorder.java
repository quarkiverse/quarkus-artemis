package io.quarkus.artemis.jms.runtime;

import java.util.Objects;
import java.util.function.Supplier;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.artemis.core.runtime.*;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {

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
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisJmsWrapper wrapper) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig());
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.getAllConfigs().getOrDefault(name,
                new ArtemisBuildTimeConfig());
        return new Supplier<>() {
            @Override
            public ConnectionFactory get() {
                ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
                return Objects.requireNonNull(extractConnectionFactory(runtimeConfig, wrapper));
            }
        };
    }

    private ConnectionFactory extractConnectionFactory(ArtemisRuntimeConfig runtimeConfig, ArtemisJmsWrapper wrapper) {
        String url = runtimeConfig.getUrl();
        if (url != null) {
            return wrapper.wrapConnectionFactory(new ActiveMQConnectionFactory(
                    url,
                    runtimeConfig.getUsername(),
                    runtimeConfig.getPassword()));
        } else {
            return null;
        }
    }
}
