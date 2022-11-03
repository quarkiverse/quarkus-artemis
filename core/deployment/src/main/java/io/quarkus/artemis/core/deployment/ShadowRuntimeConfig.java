package io.quarkus.artemis.core.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigGroup
public class ShadowRuntimeConfig {
    @ConfigItem(generateDocumentation = false)
    public Optional<String> url = Optional.empty();

    @ConfigItem(generateDocumentation = false)
    public Optional<String> username = Optional.empty();

    @ConfigItem(generateDocumentation = false)
    public Optional<String> password = Optional.empty();

    public boolean isEmpty() {
        return url.isEmpty() && username.isEmpty() && password.isEmpty();
    }
}
