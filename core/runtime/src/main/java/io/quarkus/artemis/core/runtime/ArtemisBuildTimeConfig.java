package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface ArtemisBuildTimeConfig {
    /**
     * Whether to enable this configuration.
     * <p>
     * Is enabled by default.
     */
    Optional<Boolean> enabled();

    /**
     * Configuration for DevServices. DevServices allows Quarkus to automatically start ActiveMQ Artemis in dev and test mode.
     */
    ArtemisDevServicesBuildTimeConfig devservices();

    /**
     * Support to expose {@link jakarta.jms.XAConnectionFactory}. Is not activated by default.
     */
    Optional<Boolean> xaEnabled();

    default boolean isEnabled() {
        return enabled().orElse(true);
    }

    default boolean isDisabled() {
        return !isEnabled();
    }

    default ArtemisDevServicesBuildTimeConfig getDevservices() {
        return devservices();
    }

    default boolean isXaEnabled() {
        return xaEnabled().orElse(false);
    }

    default boolean isEmpty() {
        return enabled().isEmpty() && devservices().isEmpty() && xaEnabled().isEmpty();
    }

    default boolean isPresent() {
        return !isEmpty();
    }
}
