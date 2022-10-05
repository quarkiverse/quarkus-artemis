package io.quarkus.artemis.core.runtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.runtime.annotations.*;

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

    public ArtemisRuntimeConfig getDefaultConfig() {
        return defaultConfig;
    }

    public Map<String, ArtemisRuntimeConfig> getNamedConfigs() {
        return namedConfigs;
    }

    public Map<String, ArtemisRuntimeConfig> getAllConfigs() {
        HashMap<String, ArtemisRuntimeConfig> allConfigs = new HashMap<>(getNamedConfigs());
        if (getDefaultConfig() != null) {
            allConfigs.put(ArtemisUtil.DEFAULT_CONFIG_NAME, getDefaultConfig());
        }
        return allConfigs;
    }
}
