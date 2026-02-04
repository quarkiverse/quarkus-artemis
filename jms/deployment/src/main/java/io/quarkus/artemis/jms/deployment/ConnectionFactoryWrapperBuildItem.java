package io.quarkus.artemis.jms.deployment;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * A MultiBuildItem that allows multiple extensions to contribute ConnectionFactory wrappers.
 * All wrappers will be composed together in the order they are produced.
 */
public final class ConnectionFactoryWrapperBuildItem extends MultiBuildItem {

    private final Function<ConnectionFactory, Object> wrapper;

    public ConnectionFactoryWrapperBuildItem(Function<ConnectionFactory, Object> wrapper) {
        this.wrapper = wrapper;
    }

    public Function<ConnectionFactory, Object> getWrapper() {
        return wrapper;
    }
}
