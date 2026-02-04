package io.quarkus.it.artemis.jms.opentelemetry;

/**
 * Marker interface for span exporters.
 * This allows the endpoint to remain independent of the OpenTelemetry SDK testing library.
 */
public interface SpanExporter {
    Object getDelegate();
}
