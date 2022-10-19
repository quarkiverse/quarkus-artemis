package io.quarkus.artemis.core.runtime;

import java.util.Map;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class ShadowRunTimeConfigs {
    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    public ArtemisRuntimeConfigs delegate = new ArtemisRuntimeConfigs();

    public Map<String, ArtemisRuntimeConfig> getNamedConfigs() {
        return delegate.getNamedConfigs();
    }

    public Map<String, ArtemisRuntimeConfig> getAllConfigs() {
        return delegate.getAllConfigs();
    }

    public Set<String> getNames() {
        return getNamedConfigs().keySet();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
