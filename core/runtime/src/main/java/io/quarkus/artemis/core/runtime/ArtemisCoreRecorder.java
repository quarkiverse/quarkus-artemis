package io.quarkus.artemis.core.runtime;

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
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig());
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.getAllConfigs().getOrDefault(name,
                new ArtemisBuildTimeConfig());
        ArtemisUtil.validateIntegrity(runtimeConfig, buildTimeConfig, name);
        return new Supplier<>() {
            @Override
            public ServerLocator get() {
                try {
                    return ActiveMQClient.createServerLocator(runtimeConfig.getUrl());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
