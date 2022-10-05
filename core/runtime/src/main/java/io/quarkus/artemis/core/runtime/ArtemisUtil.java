package io.quarkus.artemis.core.runtime;

public class ArtemisUtil {
    public static final String DEFAULT_CONFIG_NAME = "<default>";

    private ArtemisUtil() {
    }

    public static boolean isDefault(String configName) {
        return DEFAULT_CONFIG_NAME.equals(configName);
    }

    public static void validateIntegrity(
            ArtemisRuntimeConfig runtimeConfig,
            ArtemisBuildTimeConfig buildTimeConfig,
            String name) {
        final boolean devServiceEnabled = buildTimeConfig.getDevservices().isEnabled();
        if (runtimeConfig.getUrl() == null && devServiceEnabled) {
            throw new IllegalStateException(String.format("Connection %s: url is not set and devservices is " +
                    "activated. This is a bug. Please report it.", name));
        }
        if (runtimeConfig.getUrl() == null && !devServiceEnabled) {
            throw new IllegalStateException(String.format("Connection %s: url is not set and devservices are not " +
                    "activated. Please set either the url or activate devservices.", name));
        }
    }
}
