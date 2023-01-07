package io.quarkus.artemis.core.runtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.runtime.annotations.*;

@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class ArtemisBuildTimeConfigs {
    /**
     * The default config
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public ArtemisBuildTimeConfig defaultConfig = new ArtemisBuildTimeConfig();

    /**
     * Additional named configs
     */
    @ConfigDocSection
    @ConfigDocMapKey("configuration-name")
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, ArtemisBuildTimeConfig> namedConfigs = new HashMap<>();

    /**
     * Whether a health check is published in case the smallrye-health extension is present.
     * <p>
     * This is a global setting and is not specific to a datasource.
     */
    @ConfigItem(name = "health.enabled", defaultValue = "true")
    public boolean healthEnabled = true;

    /**
     * Whether camel context enhancement should be enabled.
     * <p>
     * If enabled, all {@link javax.jms.ConnectionFactory} s annotated with
     * {@link io.smallrye.common.annotation.Identifier} are registered as named beans in the camel
     * context.
     */
    @ConfigItem(name = "camel-quarkus-enhance-enabled")
    public boolean camelQuarkusEnhanceEnable = false;

    public ArtemisBuildTimeConfig getDefaultConfig() {
        return defaultConfig;
    }

    private Map<String, ArtemisBuildTimeConfig> getNamedConfigs() {
        return namedConfigs;
    }

    public Map<String, ArtemisBuildTimeConfig> getAllConfigs() {
        HashMap<String, ArtemisBuildTimeConfig> allConfigs = new HashMap<>(getNamedConfigs());
        if (getDefaultConfig() != null && !getDefaultConfig().isEmpty()) {
            allConfigs.put(ArtemisUtil.DEFAULT_CONFIG_NAME, getDefaultConfig());
        }
        return allConfigs;
    }

    public boolean isHealthEnabled() {
        return healthEnabled;
    }

    public boolean isEmpty() {
        return defaultConfig.isEmpty()
                && namedConfigs.isEmpty();
    }

    public boolean isCamelQuarkusEnhanceEnable() {
        return camelQuarkusEnhanceEnable;
    }
}
