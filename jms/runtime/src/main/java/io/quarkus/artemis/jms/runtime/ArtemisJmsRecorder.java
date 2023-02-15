package io.quarkus.artemis.jms.runtime;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.artemis.core.runtime.*;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {

    public Function<ConnectionFactory, Object> getDefaultWrapper() {
        return cf -> cf;
    }

    public Supplier<ConnectionFactory> getConnectionFactoryProducer(
            String name,
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            Function<ConnectionFactory, Object> wrapper) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig());
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.getAllConfigs().getOrDefault(name,
                new ArtemisBuildTimeConfig());
        return () -> {
            ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
            return Objects.requireNonNull(extractConnectionFactory(runtimeConfig, wrapper));
        };
    }

    private ConnectionFactory extractConnectionFactory(ArtemisRuntimeConfig runtimeConfig,
            Function<ConnectionFactory, Object> wrapper) {
        String url = runtimeConfig.getUrl();
        if (url != null) {
            return (ConnectionFactory) wrapper.apply(new ActiveMQConnectionFactory(
                    url,
                    runtimeConfig.getUsername(),
                    runtimeConfig.getPassword()));
        } else {
            return null;
        }
    }
}
