package io.quarkus.artemis.jms.runtime;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
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
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        final ConnectionFactory connectionFactory = Objects.requireNonNull(extractConnectionFactory(runtimeConfig, wrapper));
        return () -> connectionFactory;
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
