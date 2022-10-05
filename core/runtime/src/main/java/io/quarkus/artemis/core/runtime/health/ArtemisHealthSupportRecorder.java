package io.quarkus.artemis.core.runtime.health;

import java.util.*;
import java.util.function.Supplier;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisHealthSupportRecorder {
    public Supplier<ArtemisHealthSupport> getArtemisSupportBuilder(
            Set<String> names,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Set<String> excludedNames = processConfigs(names, buildTimeConfigs);
        return new Supplier<>() {
            @Override
            public ArtemisHealthSupport get() {
                return new ArtemisHealthSupport(names, excludedNames);
            }
        };
    }

    private static Set<String> processConfigs(
            Set<String> names,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Set<String> excluded = new HashSet<>();
        Map<String, ArtemisBuildTimeConfig> allBuildTimeConfigs = Optional.ofNullable(buildTimeConfigs.getAllConfigs())
                .orElse(Map.of());
        for (String name : names) {
            ArtemisBuildTimeConfig buildTimeConfig = allBuildTimeConfigs.getOrDefault(name, new ArtemisBuildTimeConfig());
            if (buildTimeConfig.isHealthExclude()) {
                excluded.add(name);
            }
        }
        return excluded;
    }
}
