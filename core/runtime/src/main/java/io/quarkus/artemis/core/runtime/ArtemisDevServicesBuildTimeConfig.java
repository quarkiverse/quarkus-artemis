package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class ArtemisDevServicesBuildTimeConfig {

    /**
     * Enable or disable Dev Services explicitly. Dev Services are automatically enabled unless
     * {@code quarkus.artemis.url} is set.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    Optional<Boolean> enabled = Optional.empty();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    Optional<Integer> port = Optional.empty();

    /**
     * The ActiveMQ Artemis container image to use.
     */
    @ConfigItem(defaultValue = "quay.io/artemiscloud/activemq-artemis-broker:1.0.5")
    String imageName = "quay.io/artemiscloud/activemq-artemis-broker:1.0.5";

    /**
     * Indicates if the ActiveMQ Artemis broker managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for ActiveMQ Artemis starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-artemis} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(defaultValue = "true")
    boolean shared = true;

    /**
     * The value of the {@code quarkus-dev-service-artemis} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for ActiveMQ Artemis looks for a container with the
     * {@code quarkus-dev-service-artemis} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-artemis} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared ActiveMQ Artemis brokers.
     */
    @ConfigItem(defaultValue = "artemis")
    String serviceName = "artemis";

    /**
     * User to start artemis broker
     */
    @ConfigItem(defaultValue = "admin")
    String user = "admin";

    /**
     * Password to start artemis broker
     */
    @ConfigItem(defaultValue = "admin")
    String password = "admin";

    /**
     * The value of the {@code AMQ_EXTRA_ARGS} environment variable to pass to the container.
     */
    @ConfigItem(defaultValue = "--no-autotune --mapped --no-fsync")
    String extraArgs = "--no-autotune --mapped --no-fsync";

    public boolean isEnabled() {
        return enabled.orElse(true);
    }

    public int getPort() {
        return port.orElse(0);
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isShared() {
        return shared;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getExtraArgs() {
        return extraArgs;
    }
}
