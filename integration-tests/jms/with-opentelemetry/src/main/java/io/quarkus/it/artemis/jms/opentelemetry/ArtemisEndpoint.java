package io.quarkus.it.artemis.jms.opentelemetry;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

@Path("/artemis")
public class ArtemisEndpoint {
    private final ArtemisJmsProducerManager producer;
    private final ArtemisJmsConsumerManager consumer;
    private final Instance<SpanExporter> spanExporterInstance;

    public ArtemisEndpoint(
            ArtemisJmsProducerManager producer,
            ArtemisJmsConsumerManager consumer,
            Instance<SpanExporter> spanExporterInstance) {
        this.producer = producer;
        this.consumer = consumer;
        this.spanExporterInstance = spanExporterInstance;
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
    @SuppressWarnings("unchecked")
    public List<SpanInfo> getSpans() {
        // Return empty list if span exporter is not available (e.g., in native image)
        if (!spanExporterInstance.isResolvable()) {
            return Collections.emptyList();
        }

        try {
            SpanExporter spanExporter = spanExporterInstance.get();
            Object delegate = spanExporter.getDelegate();

            // Use reflection to call getFinishedSpanItems()
            Method getFinishedSpanItems = delegate.getClass().getMethod("getFinishedSpanItems");
            getFinishedSpanItems.setAccessible(true);
            List<?> spanItems = (List<?>) getFinishedSpanItems.invoke(delegate);

            return spanItems.stream()
                    .map(span -> {
                        try {
                            Method getName = span.getClass().getMethod("getName");
                            getName.setAccessible(true);
                            Method getKind = span.getClass().getMethod("getKind");
                            getKind.setAccessible(true);
                            Method getAttributes = span.getClass().getMethod("getAttributes");
                            getAttributes.setAccessible(true);

                            String name = (String) getName.invoke(span);
                            Object kind = getKind.invoke(span);
                            Method kindNameMethod = kind.getClass().getMethod("name");
                            kindNameMethod.setAccessible(true);
                            String kindName = kindNameMethod.invoke(kind).toString();
                            Object attributes = getAttributes.invoke(span);

                            Method asMap = attributes.getClass().getMethod("asMap");
                            asMap.setAccessible(true);
                            Map<?, ?> attrMap = (Map<?, ?>) asMap.invoke(attributes);

                            Map<String, String> attrs = attrMap.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            e -> {
                                                try {
                                                    Method getKey = e.getKey().getClass().getMethod("getKey");
                                                    getKey.setAccessible(true);
                                                    return (String) getKey.invoke(e.getKey());
                                                } catch (Exception ex) {
                                                    return String.valueOf(e.getKey());
                                                }
                                            },
                                            e -> String.valueOf(e.getValue())));

                            return new SpanInfo(name, kindName, attrs);
                        } catch (Exception e) {
                            throw new RuntimeException("Error processing span", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting spans", e);
        }
    }

    @DELETE
    @Path("/spans")
    public void resetSpans() {
        // Only reset if span exporter is available
        if (spanExporterInstance.isResolvable()) {
            try {
                SpanExporter spanExporter = spanExporterInstance.get();
                Object delegate = spanExporter.getDelegate();
                Method reset = delegate.getClass().getMethod("reset");
                reset.setAccessible(true);
                reset.invoke(delegate);
            } catch (Exception e) {
                throw new RuntimeException("Error resetting spans", e);
            }
        }
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
