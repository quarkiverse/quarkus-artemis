package io.quarkus.artemis.jms.runtime;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

/**
 * A non-generic wrapper around a JMS {@link ConnectionFactory}.
 * <p>
 * Service types must not be generic, so this type is used as the service type for
 * connection factory wrappers.
 */
@FunctionalInterface
public interface ConnectionFactoryWrapper extends Function<ConnectionFactory, Object> {
}
