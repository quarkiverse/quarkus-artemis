package io.quarkus.artemis.core.runtime.health;

import java.util.Set;

public class ArtemisHealthSupport {
    private final Set<String> configuredNames;
    private final Set<String> excludedNames;

    public ArtemisHealthSupport(Set<String> configuredNames, Set<String> excludedNames) {
        this.configuredNames = configuredNames;
        this.excludedNames = excludedNames;
    }

    public Set<String> getConfiguredNames() {
        return configuredNames;
    }

    public Set<String> getExcludedNames() {
        return excludedNames;
    }
}
