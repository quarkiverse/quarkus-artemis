package io.quarkus.it.artemis.jms.opentelemetry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
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
    private final ConnectionFactory connectionFactory;
    private final InMemorySpanExporter spanExporter;

    public ArtemisEndpoint(
            ArtemisJmsProducerManager producer,
            ArtemisJmsConsumerManager consumer,
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory,
            InMemorySpanExporter spanExporter) {
        this.producer = producer;
        this.consumer = consumer;
        this.connectionFactory = connectionFactory;
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

    /**
     * Send a message using the classic JMS 1.1 Connection/Session API.
     */
    @POST
    @Path("/classic")
    @Consumes(MediaType.TEXT_PLAIN)
    public void postClassic(String message) throws Exception {
        try (Connection connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("test-jms-otel");
            MessageProducer messageProducer = session.createProducer(queue);
            TextMessage textMessage = session.createTextMessage(message);
            messageProducer.send(textMessage);
        }
    }

    /**
     * Receive a message using the classic JMS 1.1 Connection/Session API.
     */
    @GET
    @Path("/classic")
    @Produces(MediaType.TEXT_PLAIN)
    public String getClassic() throws Exception {
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("test-jms-otel");
            MessageConsumer messageConsumer = session.createConsumer(queue);
            TextMessage message = (TextMessage) messageConsumer.receive(1000L);
            return message != null ? message.getText() : null;
        }
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
                    List<LinkInfo> links = span.getLinks().stream()
                            .map(link -> new LinkInfo(
                                    link.getSpanContext().getTraceId(),
                                    link.getSpanContext().getSpanId()))
                            .collect(Collectors.toList());
                    return new SpanInfo(
                            span.getName(),
                            span.getKind().name(),
                            span.getSpanContext().getTraceId(),
                            span.getSpanContext().getSpanId(),
                            attrs,
                            links);
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
        public String traceId;
        public String spanId;
        public Map<String, String> attributes;
        public List<LinkInfo> links;

        public SpanInfo() {
        }

        public SpanInfo(String name, String kind, String traceId, String spanId,
                Map<String, String> attributes, List<LinkInfo> links) {
            this.name = name;
            this.kind = kind;
            this.traceId = traceId;
            this.spanId = spanId;
            this.attributes = attributes;
            this.links = links;
        }
    }

    public static class LinkInfo {
        public String traceId;
        public String spanId;

        public LinkInfo() {
        }

        public LinkInfo(String traceId, String spanId) {
            this.traceId = traceId;
            this.spanId = spanId;
        }
    }
}
