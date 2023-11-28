package io.quarkus.artemis.core.runtime;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisCoreRecorder {
    public Supplier<ServerLocator> getServerLocatorSupplier(
            String name,
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        ServerLocator serverLocator = Objects.requireNonNull(getServerLocator(runtimeConfig.getUrl()));
        return () -> serverLocator;
    }

    protected static ServerLocator getServerLocator(String url) {
        if (url != null) {
            try {
                return ActiveMQClient.createServerLocator(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}
