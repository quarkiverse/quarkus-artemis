package io.quarkus.artemis.core.deployment;

import java.util.Collections;
import java.util.Set;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ArtemisBootstrappedBuildItem extends SimpleBuildItem {
    private final Set<String> connectionNames;

    public ArtemisBootstrappedBuildItem(Set<String> connectionNames) {
        this.connectionNames = Collections.unmodifiableSet(connectionNames);
    }

    public Set<String> getConnectionNames() {
        return connectionNames;
    }
}
