package io.quarkus.artemis.core.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
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
    @WithUnnamedKey(ArtemisConstants.DEFAULT_CONFIG_NAME)
    Map<String, ArtemisRuntimeConfig> configs();

    /**
     * Whether configurations ({@link org.apache.activemq.artemis.api.core.client.ServerLocator}s in case of the
     * {@code artemis-core} extension, {@link jakarta.jms.ConnectionFactory}s in case of the
     * {@code artemis-jms} extension) should be included in the health check. Defaults to {@code true} if not set.
     */
    @WithName("health.external.enabled")
    Optional<Boolean> healthExternalEnabled();

    /**
     * Whether failed health checks should report the stack trace via a log.
     */
    @WithName("health.fail.log")
    @WithDefault("true")
    boolean healthFailLog();

    /**
     * The log level for the stack trace of a failed health check.
     * <p>
     * If the log level is not enabled, the health check will only report {@code "DOWN"}.
     * <p>
     * If the log level is enabled,
     * <ul>
     * <li>the health check will report {@code "DOWN, see error-id <id>"} and</li>
     * <li>the stack trace is logged on the defined log level. The log will contain the {@code error-id <id>} in the log message
     * and in the MDC.</li>
     * </ul>
     */
    @WithName("health.fail.log-level")
    @WithDefault("INFO")
    Logger.Level healthFailLogLevel();

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
