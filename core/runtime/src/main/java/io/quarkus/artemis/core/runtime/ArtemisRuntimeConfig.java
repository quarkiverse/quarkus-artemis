package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigGroup
public class ArtemisRuntimeConfig {
    /**
     * Artemis connection url
     */
    @ConfigItem
    public Optional<String> url = Optional.empty();

    /**
     * Username for authentication, only used with JMS
     */
    @ConfigItem
    public Optional<String> username = Optional.empty();

    /**
     * Password for authentication, only used with JMS
     */
    @ConfigItem
    public Optional<String> password = Optional.empty();

    public String getUrl() {
        return url.orElse(null);
    }

    public String getUsername() {
        return username.orElse(null);
    }

    public String getPassword() {
        return password.orElse(null);
    }

    public boolean isEmpty() {
        return url.isEmpty() && username.isEmpty() && password.isEmpty();
    }

    public boolean isPresent() {
        return !isEmpty();
    }
}
