package io.quarkus.artemis.jms.runtime;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.artemis.jms.runtime.tracing.TracingConnectionFactory;

/**
 * Wrapper function that adds OpenTelemetry tracing to JMS ConnectionFactory.
 * This bean is only registered when OpenTelemetry is enabled.
 */
public class ArtemisJmsOpenTelemetryWrapper implements Function<ConnectionFactory, ConnectionFactory> {

    private final OpenTelemetry openTelemetry;

    public ArtemisJmsOpenTelemetryWrapper(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public ConnectionFactory apply(ConnectionFactory connectionFactory) {
        return new TracingConnectionFactory(connectionFactory, openTelemetry);
    }
}
