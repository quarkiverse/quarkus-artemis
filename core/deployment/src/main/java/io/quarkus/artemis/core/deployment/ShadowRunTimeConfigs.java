package io.quarkus.artemis.core.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.runtime.annotations.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_TIME)
public class ShadowRunTimeConfigs {
    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    public ShadowRuntimeConfig defaultConfig;

    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    public Map<String, ShadowRuntimeConfig> namedConfigs = new HashMap<>();

    @ConfigItem(name = "health.external.enabled", generateDocumentation = false)
    public Optional<Boolean> healthExternalEnabled = Optional.empty();

    public ShadowRuntimeConfig getDefaultConfig() {
        return defaultConfig;
    }

    private Map<String, ShadowRuntimeConfig> getNamedConfigs() {
        return namedConfigs;
    }

    public Map<String, ShadowRuntimeConfig> getAllConfigs() {
        HashMap<String, ShadowRuntimeConfig> allConfigs = new HashMap<>(getNamedConfigs());
        if (getDefaultConfig() != null && !getDefaultConfig().isEmpty()) {
            allConfigs.put(ArtemisUtil.DEFAULT_CONFIG_NAME, getDefaultConfig());
        }
        return allConfigs;
    }

    public Set<String> getNames() {
        return getNamedConfigs().keySet();
    }

    public boolean isEmpty() {
        return defaultConfig.isEmpty()
                && namedConfigs.isEmpty()
                && healthExternalEnabled.isEmpty();
    }
}
