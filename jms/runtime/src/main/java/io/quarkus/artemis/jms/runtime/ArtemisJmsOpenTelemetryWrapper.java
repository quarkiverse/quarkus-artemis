package io.quarkus.artemis.jms.runtime;

import jakarta.jms.ConnectionFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.artemis.jms.runtime.tracing.TracingConnectionFactory;

/**
 * Wrapper function that adds OpenTelemetry tracing to JMS ConnectionFactory.
 * This bean is only registered when OpenTelemetry is enabled.
 * OpenTelemetry is resolved lazily from the Arc container to avoid
 * initialization ordering issues with synthetic beans.
 */
public class ArtemisJmsOpenTelemetryWrapper implements ConnectionFactoryWrapper {

    /**
     * Creates a runtime wrapper that applies OpenTelemetry tracing when the SDK is enabled.
     * The wrapper is resolved through the Arc container on first use so that the
     * {@link ArtemisJmsOpenTelemetryWrapper} bean can be registered only when OpenTelemetry
     * is present on the classpath.
     *
     * @param enabled whether the OpenTelemetry SDK is enabled at runtime
     * @return the wrapper function
     */
    public static ConnectionFactoryWrapper createWrapper(boolean enabled) {
        return cf -> {
            if (!enabled) {
                return cf;
            }
            var container = io.quarkus.arc.Arc.container();
            var wrapperInstance = container.instance(ArtemisJmsOpenTelemetryWrapper.class);
            if (wrapperInstance.isAvailable()) {
                return wrapperInstance.get().apply(cf);
            }
            return cf;
        };
    }

    @Override
    public ConnectionFactory apply(ConnectionFactory connectionFactory) {
        return new TracingConnectionFactory(connectionFactory,
                () -> io.quarkus.arc.Arc.container().instance(OpenTelemetry.class).get());
    }
}
