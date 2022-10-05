package io.quarkus.artemis.core.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class ShadowRunTimeConfig {

    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    public ArtemisRuntimeConfigs delegate = new ArtemisRuntimeConfigs();

    public ArtemisRuntimeConfig getDefaultConfig() {
        return delegate.getDefaultConfig();
    }

    public Map<String, ArtemisRuntimeConfig> getNamedConfigs() {
        return delegate.getNamedConfigs();
    }

    public Map<String, ArtemisRuntimeConfig> getAllConfigs() {
        return delegate.getAllConfigs();
    }

    public Set<String> getNames() {
        HashSet<String> names = new HashSet<>();
        if (getDefaultConfig().isEnabled()) {
            names.add(ArtemisUtil.DEFAULT_CONFIG_NAME);
        }
        for (var item : getNamedConfigs().entrySet()) {
            if (item.getValue().isEnabled()) {
                names.add(item.getKey());
            }
        }
        return names;
    }
}
