package io.quarkus.artemis.core.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.artemis")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface ArtemisBuildTimeConfigs {
    /**
     * Configurations
     */
    @ConfigDocSection
    @ConfigDocMapKey("configuration-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(ArtemisUtil.DEFAULT_CONFIG_NAME)
    Map<String, ArtemisBuildTimeConfig> configs();

    /**
     * Whether a health check is published in case the smallrye-health extension is present.
     * <p>
     * This is a global setting and is not specific to a datasource.
     */
    @WithName("health.enabled")
    Optional<Boolean> healthEnabled();

    default Set<String> getNames() {
        HashSet<String> names = new HashSet<>();
        for (var entry : configs().entrySet()) {
            if (entry.getValue().isPresent()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    default boolean isHealthEnabled() {
        return healthEnabled().orElse(true);
    }

    default boolean isEmpty() {
        Boolean hasNoConfig = configs().values().stream()
                .map(ArtemisBuildTimeConfig::isEmpty)
                .reduce(true, Boolean::logicalAnd);
        return hasNoConfig && healthEnabled().isEmpty();
    }
}
