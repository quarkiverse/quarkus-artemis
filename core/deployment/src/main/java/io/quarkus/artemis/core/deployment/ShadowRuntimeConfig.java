package io.quarkus.artemis.core.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocIgnore;
import io.quarkus.runtime.annotations.ConfigGroup;

/**
 * This class is a build-time mirror/shadow of {@link io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig} to make the
 * configuration properties accessible at build-time.
 *
 * <p>
 * We use this configuration to access the configuration structure, and to extract the names of configurations
 * present at build-time, so we can create the {@link org.apache.activemq.artemis.api.core.client.ServerLocator} /
 * {@link jakarta.jms.ConnectionFactory} beans. Most importantly, we only query the presence of keys and/or values,
 * we do not access values, since they could change at runtime.
 */
@ConfigGroup
public interface ShadowRuntimeConfig {
    @ConfigDocIgnore
    Optional<String> url();

    @ConfigDocIgnore
    Optional<String> username();

    @ConfigDocIgnore
    Optional<String> password();

    @ConfigDocIgnore
    Optional<Boolean> healthExclude();

    default boolean isUrlEmpty() {
        return url().isEmpty();
    }

    default boolean isEmpty() {
        return url().isEmpty() && username().isEmpty() && password().isEmpty() && healthExclude().isEmpty();
    }

    default boolean isPresent() {
        return !isEmpty();
    }
}
