package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigGroup
public class ArtemisBuildTimeConfig {
    /**
     * Whether to enable this configuration.
     * <p>
     * Is enabled by default.
     */
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * Configuration for DevServices. DevServices allows Quarkus to automatically start ActiveMQ Artemis in dev and test mode.
     */
    @ConfigItem
    public ArtemisDevServicesBuildTimeConfig devservices = new ArtemisDevServicesBuildTimeConfig();

    /**
     * Whether this particular data source should be excluded from the health check if
     * the general health check for data sources is enabled.
     * <p>
     * By default, the health check includes all configured data sources (if it is enabled).
     */
    @ConfigItem
    public Optional<Boolean> healthExclude = Optional.empty();

    /**
     * Support to expose {@link jakarta.jms.XAConnectionFactory}. Is not activated by default.
     */
    @ConfigItem
    public Optional<Boolean> xaEnabled = Optional.empty();

    public boolean isEnabled() {
        return enabled.orElse(true);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public ArtemisDevServicesBuildTimeConfig getDevservices() {
        return devservices;
    }

    public boolean isHealthExclude() {
        return healthExclude.orElse(false);
    }

    public boolean isXaEnabled() {
        return xaEnabled.orElse(false);
    }

    public boolean isEmpty() {
        return enabled.isEmpty() && devservices.isEmpty() && healthExclude.isEmpty() && xaEnabled.isEmpty();
    }

    public boolean isPresent() {
        return !isEmpty();
    }
}
