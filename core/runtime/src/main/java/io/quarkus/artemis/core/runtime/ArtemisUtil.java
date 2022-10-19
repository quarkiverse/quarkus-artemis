package io.quarkus.artemis.core.runtime;

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
}
