package io.quarkus.artemis.core.runtime;

import java.lang.annotation.Annotation;
import java.util.*;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.inject.Any;

public class ArtemisUtil {
    public static final String DEFAULT_CONFIG_NAME = "<default>";

    private ArtemisUtil() {
    }

    public static boolean isDefault(String configName) {
        return DEFAULT_CONFIG_NAME.equals(configName);
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

    public static <T> Map<String, T> extractIdentifiers(Class<T> clazz, Set<String> namesToIgnore) {
        HashMap<String, T> connectionFactoryNamesFromArc = new HashMap<>();
        for (InstanceHandle<T> handle : Arc.container().listAll(clazz, Any.Literal.INSTANCE)) {
            String name = extractIdentifier(handle);
            if (name != null && !namesToIgnore.contains(name)) {
                connectionFactoryNamesFromArc.put(name, handle.get());
            }
        }
        return connectionFactoryNamesFromArc;
    }

    private static String extractIdentifier(InstanceHandle<?> handle) {
        for (Annotation qualifier : handle.getBean().getQualifiers()) {
            if (qualifier instanceof Identifier) {
                Identifier identifier = (Identifier) qualifier;
                return identifier.value();
            }
        }
        return null;
    }
}
