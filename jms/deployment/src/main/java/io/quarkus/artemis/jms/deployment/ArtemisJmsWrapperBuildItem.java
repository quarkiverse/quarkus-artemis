package io.quarkus.artemis.jms.deployment;

import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.builder.item.SimpleBuildItem;

/**
 * It holds {@link ArtemisJmsWrapper} for integration with pooling and transaction support
 */
public final class ArtemisJmsWrapperBuildItem extends SimpleBuildItem {
    private final ArtemisJmsWrapper wrapper;

    public ArtemisJmsWrapperBuildItem(ArtemisJmsWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public ArtemisJmsWrapper getWrapper() {
        return wrapper;
    }
}
