package io.quarkus.artemis.core.runtime;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;

import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.common.annotation.Identifier;

public class ArtemisUtil {
    /**
     * @deprecated use {@link ArtemisConstants#DEFAULT_CONFIG_NAME} instead.
     */
    @Deprecated(since = "3.10.4", forRemoval = true)
    public static final String DEFAULT_CONFIG_NAME = ArtemisConstants.DEFAULT_CONFIG_NAME;

    /**
     * @deprecated use {@link ArtemisConstants#ERROR_ID_KEY} instead.
     */
    @Deprecated(since = "3.10.4", forRemoval = true)
    public static final String ERROR_ID_KEY = ArtemisConstants.ERROR_ID_KEY;

    private ArtemisUtil() {
    }

    public static boolean isDefault(String configName) {
        return ArtemisConstants.DEFAULT_CONFIG_NAME.equals(configName);
    }

    public static void validateIntegrity(
            String name,
            ArtemisRuntimeConfig runtimeConfig,
            ArtemisBuildTimeConfig buildTimeConfig) {
        final boolean devServiceEnabled = buildTimeConfig.getDevservices().isEnabled();
        if (runtimeConfig.getUrl() == null && devServiceEnabled) {
            throw new IllegalStateException(String.format(
                    "Configuration %s: url is not set and devservices is activated. This is a bug. Please report it.", name));
        }
        if ((runtimeConfig.isPresent() || buildTimeConfig.isPresent()) && buildTimeConfig.isEnabled()
                && runtimeConfig.getUrl() == null) {
            throw new IllegalStateException(String.format(
                    "Configuration %s: the configuration is enabled, but no URL is configured. Please either disable the configuration or set the URL.",
                    name));
        }
    }

    public static <T> Set<String> extractIdentifiers(Class<T> clazz, Set<String> namesToIgnore) {
        Set<String> names = new HashSet<>();
        for (InstanceHandle<T> handle : Arc.container().listAll(clazz, Any.Literal.INSTANCE)) {
            String name = extractIdentifier(handle);
            if (name != null && !namesToIgnore.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    private static String extractIdentifier(InstanceHandle<?> handle) {
        for (Annotation qualifier : handle.getBean().getQualifiers()) {
            if (qualifier instanceof Identifier identifier) {
                return identifier.value();
            }
        }
        return null;
    }

    public static <T> Set<String> getExternalNames(Class<T> type, ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Set<String> externalNames = new HashSet<>();
        if (runtimeConfigs.getHealthExternalEnabled()) {
            HashSet<String> namesToIgnore = new HashSet<>(runtimeConfigs.getNames());
            namesToIgnore.addAll(buildTimeConfigs.getNames());
            externalNames.addAll(ArtemisUtil.extractIdentifiers(type, namesToIgnore));
        }
        return externalNames;
    }

    public static Annotation toIdentifier(String name) {
        if (isDefault(name)) {
            return Default.Literal.INSTANCE;
        } else {
            return Identifier.Literal.of(name);
        }
    }

    public static void handleFailedHealthCheck(HealthCheckResponseBuilder builder, String connectionKind, String name,
            Logger logger, boolean doLog, Logger.Level logLevel, Throwable t) {
        if (doLog && logger.isEnabled(logLevel)) {
            String errorId = UUID.randomUUID().toString();
            MDC.put(ArtemisConstants.ERROR_ID_KEY, errorId);
            logger.log(logLevel,
                    "Exception occurred during health check of %s %s (%s %s)".formatted(
                            connectionKind, name, ArtemisConstants.ERROR_ID_KEY, errorId),
                    t);
            MDC.remove(ArtemisConstants.ERROR_ID_KEY);
            builder.withData(name, "DOWN, see %s %s".formatted(ArtemisConstants.ERROR_ID_KEY, errorId)).down();
        } else {
            builder.withData(name, "DOWN").down();
        }
    }
}
