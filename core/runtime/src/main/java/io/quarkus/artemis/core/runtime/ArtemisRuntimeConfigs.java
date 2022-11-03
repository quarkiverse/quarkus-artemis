package io.quarkus.artemis.core.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigGroup
@ConfigRoot(name = "artemis", phase = ConfigPhase.RUN_TIME)
public class ArtemisRuntimeConfigs {
    /**
     * The default configuration
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public ArtemisRuntimeConfig defaultConfig;

    /**
     * Additional named configuration
     */
    @ConfigDocSection
    @ConfigDocMapKey("configuration-name")
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, ArtemisRuntimeConfig> namedConfigs = new HashMap<>();

    /**
     * Whether configurations ({@link org.apache.activemq.artemis.api.core.client.ServerLocator}s in case of the
     * {@code artemis-core} extension, {@link javax.jms.ConnectionFactory}s in case of the
     * {@code artemis-jms} extension) should be included in the health check. Defaults to {@code true} if not set.
     */
    @ConfigItem(name = "health.external.enabled")
    public Optional<Boolean> healthExternalEnabled = Optional.empty();

    public ArtemisRuntimeConfig getDefaultConfig() {
        return defaultConfig;
    }

    private Map<String, ArtemisRuntimeConfig> getNamedConfigs() {
        return namedConfigs;
    }

    public boolean getHealthExternalEnabled() {
        return healthExternalEnabled.orElse(true);
    }

    public Map<String, ArtemisRuntimeConfig> getAllConfigs() {
        HashMap<String, ArtemisRuntimeConfig> allConfigs = new HashMap<>(getNamedConfigs());
        if (getDefaultConfig() != null && !getDefaultConfig().isEmpty()) {
            allConfigs.put(ArtemisUtil.DEFAULT_CONFIG_NAME, getDefaultConfig());
        }
        return allConfigs;
    }

    public boolean isEmpty() {
        return defaultConfig.isEmpty()
                && namedConfigs.isEmpty()
                && healthExternalEnabled.isEmpty();
    }
}
