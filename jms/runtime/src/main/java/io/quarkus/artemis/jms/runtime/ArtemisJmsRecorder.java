package io.quarkus.artemis.jms.runtime;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {
    private final ArtemisBuildTimeConfigs buildTimeConfigs;
    private final RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs;

    public ArtemisJmsRecorder(ArtemisBuildTimeConfigs buildTimeConfigs, RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs) {
        this.buildTimeConfigs = buildTimeConfigs;
        this.runtimeConfigs = runtimeConfigs;
    }

    public Function<ConnectionFactory, Object> getDefaultWrapper() {
        return cf -> cf;
    }

    public Supplier<ConnectionFactory> getConnectionFactoryProducer(String name, Function<ConnectionFactory, Object> wrapper) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getValue().configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        final ConnectionFactory connectionFactory = Objects
                .requireNonNull(extractConnectionFactory(buildTimeConfig.isXaEnabled(), runtimeConfig, wrapper));
        return () -> connectionFactory;
    }

    private ConnectionFactory extractConnectionFactory(boolean isXaEnabled, ArtemisRuntimeConfig runtimeConfig,
            Function<ConnectionFactory, Object> wrapper) {
        String url = runtimeConfig.getUrl();
        if (url != null) {
            if (isXaEnabled) {
                return (ConnectionFactory) wrapper.apply(new ActiveMQXAConnectionFactory(
                        url,
                        runtimeConfig.getUsername(),
                        runtimeConfig.getPassword()));
            } else {
                return (ConnectionFactory) wrapper.apply(new ActiveMQConnectionFactory(
                        url,
                        runtimeConfig.getUsername(),
                        runtimeConfig.getPassword()));
            }
        } else {
            return null;
        }
    }
}
