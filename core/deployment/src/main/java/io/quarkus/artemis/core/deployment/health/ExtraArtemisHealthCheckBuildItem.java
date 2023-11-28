package io.quarkus.artemis.core.deployment.health;

import io.quarkus.builder.item.MultiBuildItem;

public final class ExtraArtemisHealthCheckBuildItem extends MultiBuildItem {
    private final String name;

    public ExtraArtemisHealthCheckBuildItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
