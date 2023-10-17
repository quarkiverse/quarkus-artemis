package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface ArtemisRuntimeConfig {
    /**
     * Artemis connection url.
     */
    Optional<String> url();

    /**
     * Username for authentication, only used with JMS.
     */
    Optional<String> username();

    /**
     * Password for authentication, only used with JMS.
     */
    Optional<String> password();

    /**
     * Whether this particular data source should be excluded from the health check if
     * the general health check for data sources is enabled.
     * <p>
     * By default, the health check includes all configured data sources (if it is enabled).
     */
    Optional<Boolean> healthExclude();

    default String getUrl() {
        return url().orElse(null);
    }

    default String getUsername() {
        return username().orElse(null);
    }

    default String getPassword() {
        return password().orElse(null);
    }

    default boolean isHealthExclude() {
        return healthExclude().orElse(false);
    }

    default boolean isHealthInclude() {
        return !isHealthExclude();
    }

    default boolean isEmpty() {
        return url().isEmpty() && username().isEmpty() && password().isEmpty() && healthExclude().isEmpty();
    }

    default boolean isPresent() {
        return !isEmpty();
    }
}
