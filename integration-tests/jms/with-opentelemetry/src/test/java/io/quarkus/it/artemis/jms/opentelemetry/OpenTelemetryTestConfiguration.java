package io.quarkus.it.artemis.jms.opentelemetry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.quarkus.arc.Unremovable;

@ApplicationScoped
public class OpenTelemetryTestConfiguration {

    @Produces
    @Singleton
    @Unremovable
    public InMemorySpanExporter inMemorySpanExporter() {
        return InMemorySpanExporter.create();
    }

    @Produces
    @Singleton
    public SpanProcessor spanProcessor(InMemorySpanExporter exporter) {
        return SimpleSpanProcessor.create(exporter);
    }

    @Produces
    @Singleton
    @Unremovable
    public SpanExporter spanExporterWrapper(InMemorySpanExporter exporter) {
        return () -> exporter;
    }
}
