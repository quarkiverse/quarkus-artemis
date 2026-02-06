package io.quarkus.artemis.jms.runtime;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.artemis.jms.runtime.tracing.TracingConnectionFactory;

/**
 * Wrapper function that adds OpenTelemetry tracing to JMS ConnectionFactory.
 * This bean is only registered when OpenTelemetry is enabled.
 * OpenTelemetry is resolved lazily from the Arc container to avoid
 * initialization ordering issues with synthetic beans.
 */
public class ArtemisJmsOpenTelemetryWrapper implements Function<ConnectionFactory, ConnectionFactory> {

    @Override
    public ConnectionFactory apply(ConnectionFactory connectionFactory) {
        return new TracingConnectionFactory(connectionFactory,
                () -> io.quarkus.arc.Arc.container().instance(OpenTelemetry.class).get());
    }
}
