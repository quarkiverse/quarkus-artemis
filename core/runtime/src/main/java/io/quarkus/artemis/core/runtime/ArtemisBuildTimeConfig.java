package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class ArtemisBuildTimeConfig {

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
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public Optional<Boolean> healthExclude = Optional.empty();

    /**
     * Support to expose {@link javax.jms.XAConnectionFactory}
     */
    @ConfigItem(defaultValue = "false")
    public boolean xaEnabled;

    public ArtemisDevServicesBuildTimeConfig getDevservices() {
        return devservices;
    }

    public boolean isHealthExclude() {
        return healthExclude.orElse(false);
    }

    public boolean isXaEnabled() {
        return xaEnabled;
    }
}
