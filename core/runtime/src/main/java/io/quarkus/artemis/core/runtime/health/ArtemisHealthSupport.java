package io.quarkus.artemis.core.runtime.health;

import java.util.Set;

public class ArtemisHealthSupport {
    private final Set<String> configuredNames;

    public ArtemisHealthSupport(Set<String> configuredNames) {
        this.configuredNames = configuredNames;
    }

    public Set<String> getConfiguredNames() {
        return configuredNames;
    }
}
