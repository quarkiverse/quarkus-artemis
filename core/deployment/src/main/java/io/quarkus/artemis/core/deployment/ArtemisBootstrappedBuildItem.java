package io.quarkus.artemis.core.deployment;

import java.util.Collections;
import java.util.Set;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ArtemisBootstrappedBuildItem extends SimpleBuildItem {
    private final Set<String> configurationNames;

    public ArtemisBootstrappedBuildItem(Set<String> configurationNames) {
        this.configurationNames = Collections.unmodifiableSet(configurationNames);
    }

    public Set<String> getConfigurationNames() {
        return configurationNames;
    }
}
