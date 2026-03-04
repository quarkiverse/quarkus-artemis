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

    /**
     * Send a message to a closed connection to trigger an error span.
     * Uses send(Destination, Message) to ensure the error occurs inside the tracing wrapper.
     */
    @POST
    @Path("/error/classic")
    @Consumes(MediaType.TEXT_PLAIN)
    public jakarta.ws.rs.core.Response postErrorClassic(String message) {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("test-jms-otel");
            // Create a producer without a default destination
            MessageProducer messageProducer = session.createProducer(null);
            TextMessage textMessage = session.createTextMessage(message);
            // Close the connection so delegate.send() fails inside the tracing wrapper
            connection.close();
            messageProducer.send(queue, textMessage);
            return jakarta.ws.rs.core.Response.noContent().build();
        } catch (Exception e) {
            return jakarta.ws.rs.core.Response.status(500).entity(e.getMessage()).build();
        }
    }

    /**
     * Send a null message using JMS 2.0 API to trigger an error span.
     */
    @POST
    @Path("/error/jms2")
    public jakarta.ws.rs.core.Response postErrorJms2() {
        try (jakarta.jms.JMSContext context = connectionFactory.createContext(
                jakarta.jms.JMSContext.AUTO_ACKNOWLEDGE)) {
            jakarta.jms.JMSProducer jmsProducer = context.createProducer();
            Queue queue = context.createQueue("test-jms-otel");
            // Send a null Message to trigger an error
            jmsProducer.send(queue, (jakarta.jms.Message) null);
            return jakarta.ws.rs.core.Response.noContent().build();
        } catch (Exception e) {
            return jakarta.ws.rs.core.Response.status(500).entity(e.getMessage()).build();
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
                    String statusCode = span.getStatus().getStatusCode().name();
                    String statusDescription = span.getStatus().getDescription();
                    List<EventInfo> events = span.getEvents().stream()
                            .map(event -> {
                                Map<String, String> eventAttrs = event.getAttributes().asMap().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                e -> ((AttributeKey<?>) e.getKey()).getKey(),
                                                e -> String.valueOf(e.getValue())));
                                return new EventInfo(event.getName(), eventAttrs);
                            })
                            .collect(Collectors.toList());
                    return new SpanInfo(
                            span.getName(),
                            span.getKind().name(),
                            span.getSpanContext().getTraceId(),
                            span.getSpanContext().getSpanId(),
                            statusCode,
                            statusDescription,
                            attrs,
                            links,
                            events);
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
        public String statusCode;
        public String statusDescription;
        public Map<String, String> attributes;
        public List<LinkInfo> links;
        public List<EventInfo> events;

        public SpanInfo() {
        }

        public SpanInfo(String name, String kind, String traceId, String spanId,
                String statusCode, String statusDescription,
                Map<String, String> attributes, List<LinkInfo> links, List<EventInfo> events) {
            this.name = name;
            this.kind = kind;
            this.traceId = traceId;
            this.spanId = spanId;
            this.statusCode = statusCode;
            this.statusDescription = statusDescription;
            this.attributes = attributes;
            this.links = links;
            this.events = events;
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

    public static class EventInfo {
        public String name;
        public Map<String, String> attributes;

        public EventInfo() {
        }

        public EventInfo(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }
    }
}
