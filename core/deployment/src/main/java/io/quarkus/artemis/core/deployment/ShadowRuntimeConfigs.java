package io.quarkus.artemis.core.deployment;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.artemis.core.runtime.ArtemisUtil;
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
    /**
     * Configurations
     *
     * @deprecated since 3.1.3, to suppress doc generation
     */
    @WithParentName
    @WithUnnamedKey(ArtemisUtil.DEFAULT_CONFIG_NAME)
    @WithDefaults
    @Deprecated(since = "3.1.3")
    Map<String, ShadowRuntimeConfig> configs();

    /**
     * Whether configurations ({@link org.apache.activemq.artemis.api.core.client.ServerLocator}s in case of the
     * {@code artemis-core} extension, {@link jakarta.jms.ConnectionFactory}s in case of the
     * {@code artemis-jms} extension) should be included in the health check. Defaults to {@code true} if not set.
     *
     * @deprecated since 3.1.3, to suppress doc generation
     */
    @Deprecated(since = "3.1.3")
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
