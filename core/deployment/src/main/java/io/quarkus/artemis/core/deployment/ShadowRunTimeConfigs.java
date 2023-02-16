package io.quarkus.artemis.core.deployment;

import java.util.*;

import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.runtime.annotations.*;

/**
 * This class is a build-time mirror/shadow of {@link io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs} to make the
 * configuration properties accessible at build-time.
 *
 * <p>
 * We use this configuration to access the configuration structure, and to extract the names of configurations
 * present at build-time, so we can create the {@link org.apache.activemq.artemis.api.core.client.ServerLocator} /
 * {@link jakarta.jms.ConnectionFactory} beans. Most importantly, we only query the presence of keys and/or values,
 * we do not access values, since they could change at runtime.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_TIME)
public class ShadowRunTimeConfigs {
    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    protected ShadowRuntimeConfig defaultConfig;

    @ConfigItem(name = ConfigItem.PARENT, generateDocumentation = false)
    protected Map<String, ShadowRuntimeConfig> namedConfigs = new HashMap<>();

    @ConfigItem(name = "health.external.enabled", generateDocumentation = false)
    protected Optional<Boolean> healthExternalEnabled = Optional.empty();

    public ShadowRuntimeConfig getDefaultConfig() {
        return defaultConfig;
    }

    private Map<String, ShadowRuntimeConfig> getNamedConfigs() {
        return namedConfigs;
    }

    private Map<String, ShadowRuntimeConfig> getAllConfigs() {
        HashMap<String, ShadowRuntimeConfig> allConfigs = new HashMap<>(getNamedConfigs());
        if (getDefaultConfig() != null && !getDefaultConfig().isEmpty()) {
            allConfigs.put(ArtemisUtil.DEFAULT_CONFIG_NAME, getDefaultConfig());
        }
        return allConfigs;
    }

    public boolean isUrlEmpty(String name) {
        return getAllConfigs().getOrDefault(name, new ShadowRuntimeConfig()).isUrlEmpty();
    }

    public Set<String> getNames() {
        HashSet<String> names = new HashSet<>();
        for (var entry : getAllConfigs().entrySet()) {
            if (entry.getValue().isPresent()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    public boolean isEmpty() {
        return defaultConfig.isEmpty()
                && namedConfigs.isEmpty()
                && healthExternalEnabled.isEmpty();
    }
}
