package io.quarkus.it.artemis.jms.opentelemetry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

@Path("/artemis")
public class ArtemisEndpoint {
    private final ArtemisJmsProducerManager producer;
    private final ArtemisJmsConsumerManager consumer;
    private final InMemorySpanExporter spanExporter;

    public ArtemisEndpoint(
            ArtemisJmsProducerManager producer,
            ArtemisJmsConsumerManager consumer,
            InMemorySpanExporter spanExporter) {
        this.producer = producer;
        this.consumer = consumer;
        this.spanExporter = spanExporter;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public void post(String message) {
        producer.send(message);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return consumer.receive();
    }

    @GET
    @Path("/spans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpanInfo> getSpans() {
        return spanExporter.getFinishedSpanItems().stream()
                .map(span -> {
                    Map<String, String> attrs = span.getAttributes().asMap().entrySet().stream()
                            .collect(Collectors.toMap(
                                    e -> ((AttributeKey<?>) e.getKey()).getKey(),
                                    e -> String.valueOf(e.getValue())));
                    return new SpanInfo(span.getName(), span.getKind().name(), attrs);
                })
                .collect(Collectors.toList());
    }

    @DELETE
    @Path("/spans")
    public void resetSpans() {
        spanExporter.reset();
    }

    public static class SpanInfo {
        public String name;
        public String kind;
        public Map<String, String> attributes;

        public SpanInfo() {
        }

        public SpanInfo(String name, String kind, Map<String, String> attributes) {
            this.name = name;
            this.kind = kind;
            this.attributes = attributes;
        }
    }
}
