package io.quarkus.artemis.core.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * This class is a build-time mirror/shadow of {@link io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig} to make the
 * configuration properties accessible at build-time.
 *
 * <p>
 * We use this configuration to access the configuration structure, and to extract the names of configurations
 * present at build-time, so we can create the {@link org.apache.activemq.artemis.api.core.client.ServerLocator} /
 * {@link javax.jms.ConnectionFactory} beans. Most importantly, we only query the presence of keys and/or values, we do
 * not access values, since they could change at runtime.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigGroup
public class ShadowRuntimeConfig {
    @ConfigItem(generateDocumentation = false)
    protected Optional<String> url = Optional.empty();

    @ConfigItem(generateDocumentation = false)
    protected Optional<String> username = Optional.empty();

    @ConfigItem(generateDocumentation = false)
    protected Optional<String> password = Optional.empty();

    boolean isUrlEmpty() {
        return url.isEmpty();
    }

    public boolean isEmpty() {
        return url.isEmpty() && username.isEmpty() && password.isEmpty();
    }

    public boolean isPresent() {
        return !isEmpty();
    }
}
