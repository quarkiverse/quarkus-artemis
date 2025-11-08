package io.quarkus.artemis.core.deployment;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.artemis.core.runtime.ArtemisConstants;
import io.quarkus.runtime.annotations.ConfigDocIgnore;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

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
@ConfigMapping(prefix = "quarkus.artemis")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ShadowRuntimeConfigs {
    @WithParentName
    @WithUnnamedKey(ArtemisConstants.DEFAULT_CONFIG_NAME)
    @WithDefaults
    @ConfigDocIgnore
    Map<String, ShadowRuntimeConfig> configs();

    @ConfigDocIgnore
    @WithName("health.external.enabled")
    Optional<Boolean> healthExternalEnabled();

    default boolean isUrlEmpty(String name) {
        return configs().get(name).isUrlEmpty();
    }

    default Set<String> getNames() {
        HashSet<String> names = new HashSet<>();
        for (var entry : configs().entrySet()) {
            if (entry.getValue().isPresent()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    default boolean isEmpty() {
        Boolean hasNoConfig = configs().values().stream()
                .map(ShadowRuntimeConfig::isEmpty)
                .reduce(true, Boolean::logicalAnd);
        return hasNoConfig && healthExternalEnabled().isEmpty();
    }
}
