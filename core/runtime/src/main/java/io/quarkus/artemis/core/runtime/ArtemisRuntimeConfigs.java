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
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ArtemisRuntimeConfigs {
    /**
     * Configurations
     */
    @ConfigDocSection
    @ConfigDocMapKey("configuration-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(ArtemisUtil.DEFAULT_CONFIG_NAME)
    Map<String, ArtemisRuntimeConfig> configs();

    /**
     * Whether configurations ({@link org.apache.activemq.artemis.api.core.client.ServerLocator}s in case of the
     * {@code artemis-core} extension, {@link jakarta.jms.ConnectionFactory}s in case of the
     * {@code artemis-jms} extension) should be included in the health check. Defaults to {@code true} if not set.
     */
    @WithName("health.external.enabled")
    Optional<Boolean> healthExternalEnabled();

    default Set<String> getNames() {
        HashSet<String> names = new HashSet<>();
        for (var entry : configs().entrySet()) {
            if (entry.getValue().isPresent()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    default boolean getHealthExternalEnabled() {
        return healthExternalEnabled().orElse(true);
    }

    default boolean isEmpty() {
        Boolean hasNoConfig = configs().values().stream()
                .map(ArtemisRuntimeConfig::isEmpty)
                .reduce(true, Boolean::logicalAnd);
        return hasNoConfig && healthExternalEnabled().isEmpty();
    }
}
