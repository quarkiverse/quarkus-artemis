package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class ArtemisRuntimeConfig {

    /**
     * Whether to enable this connection.
     * <p>
     * Is enabled by default.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * Artemis connection url
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public Optional<String> url = Optional.empty();

    /**
     * Username for authentication, only used with JMS
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public Optional<String> username = Optional.empty();

    /**
     * Password for authentication, only used with JMS
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public Optional<String> password = Optional.empty();

    public boolean isEnabled() {
        return enabled.orElse(true);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public String getUrl() {
        return url.orElse(null);
    }

    public String getUsername() {
        return username.orElse(null);
    }

    public String getPassword() {
        return password.orElse(null);
    }
}
