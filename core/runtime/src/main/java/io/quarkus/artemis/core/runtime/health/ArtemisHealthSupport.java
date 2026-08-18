package io.quarkus.artemis.core.runtime.health;

import java.util.Set;

public record ArtemisHealthSupport(Set<String> configuredNames) {
}
