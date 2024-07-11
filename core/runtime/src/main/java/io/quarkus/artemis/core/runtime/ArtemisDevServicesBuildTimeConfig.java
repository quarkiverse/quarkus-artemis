package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface ArtemisDevServicesBuildTimeConfig {
    /**
     * Enable or disable Dev Services explicitly. Dev Services are automatically enabled unless
     * {@code quarkus.artemis.url} is set.
     */
    Optional<Boolean> enabled();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    Optional<Integer> port();

    /**
     * The ActiveMQ Artemis container image to use.
     * <p>
     * Defaults to {@code quay.io/artemiscloud/activemq-artemis-broker:artemis.2.34.0}
     */
    Optional<String> imageName();

    /**
     * Indicates if the ActiveMQ Artemis broker managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for ActiveMQ Artemis starts a new container. Is activated by
     * default when not set.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-artemis} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    Optional<Boolean> shared();

    /**
     * The value of the {@code quarkus-dev-service-artemis} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}. It defaults to {@code artemis} when not set.
     * In this case, before starting a container, Dev Services for ActiveMQ Artemis looks for a container with the
     * {@code quarkus-dev-service-artemis} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-artemis} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared ActiveMQ Artemis brokers.
     */
    Optional<String> serviceName();

    /**
     * User to start artemis broker. Defaults to {@code admin} if not set.
     */
    Optional<String> user();

    /**
     * Password to start artemis broker. Defaults to {@code admin} when not set.
     */
    Optional<String> password();

    /**
     * The value of the {@code AMQ_EXTRA_ARGS} environment variable to pass to the container. Defaults to
     * {@code --no-autotune --mapped --no-fsync} when not set.
     */
    Optional<String> extraArgs();

    default boolean isEnabled() {
        return enabled().orElse(true);
    }

    default int getPort() {
        return port().orElse(0);
    }

    default String getImageName() {
        return imageName().orElse("quay.io/artemiscloud/activemq-artemis-broker:artemis.2.34.0");
    }

    default boolean isShared() {
        return shared().orElse(true);
    }

    default String getServiceName() {
        return serviceName().orElse("artemis");
    }

    default String getUser() {
        return user().orElse("admin");
    }

    default String getPassword() {
        return password().orElse("admin");
    }

    default String getExtraArgs() {
        return extraArgs().orElse("--no-autotune --mapped --no-fsync");
    }

    default boolean isEmpty() {
        return enabled().isEmpty()
                && port().isEmpty()
                && imageName().isEmpty()
                && shared().isEmpty()
                && serviceName().isEmpty()
                && user().isEmpty()
                && password().isEmpty()
                && extraArgs().isEmpty();
    }
}
