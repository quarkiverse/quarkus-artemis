package io.quarkus.it.artemis.jms.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;

abstract public class BaseOpenTelemetryTest {

    @Inject
    InMemorySpanExporter spanExporter;

    @BeforeEach
    void setUp() {
        spanExporter.reset();

        // Clear any leftover messages from the queue
        try {
            String leftover;
            while ((leftover = given().when().get("/artemis").then().extract().asString()) != null
                    && !leftover.isEmpty()) {
                // Consume all messages
            }
        } catch (Exception e) {
            // Queue is empty, which is expected
        }
    }

    @Test
    void testJmsTracingProducer() {
        // Send a message
        String body = "test-message-producer-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        // Wait for spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> spanExporter.getFinishedSpanItems().size() > 0);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();

        // Find JMS producer spans
        List<SpanData> producerSpans = spans.stream()
                .filter(span -> span.getKind() == SpanKind.PRODUCER)
                .toList();

        assertThat("Should have at least one JMS producer span", producerSpans, hasSize(greaterThan(0)));

        SpanData producerSpan = producerSpans.get(0);
        assertThat("Producer span name should contain 'publish'", producerSpan.getName().contains("publish"), is(true));

        // Verify span attributes
        assertThat("Should have messaging.system attribute",
                producerSpan.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("messaging.system")),
                is(equalTo("jms")));
        assertThat("Should have messaging.destination.name attribute",
                producerSpan.getAttributes()
                        .get(io.opentelemetry.api.common.AttributeKey.stringKey("messaging.destination.name")),
                is(notNullValue()));
    }

    @Test
    void testJmsTracingConsumer() {
        // Send a message first
        String body = "test-message-consumer-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        spanExporter.reset();

        // Receive the message
        given()
                .when()
                .get("/artemis")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> spanExporter.getFinishedSpanItems().size() > 0);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();

        // Find JMS consumer spans
        List<SpanData> consumerSpans = spans.stream()
                .filter(span -> span.getKind() == SpanKind.CONSUMER)
                .toList();

        assertThat("Should have at least one JMS consumer span", consumerSpans, hasSize(greaterThan(0)));

        SpanData consumerSpan = consumerSpans.get(0);
        assertThat("Consumer span name should contain 'receive'", consumerSpan.getName().contains("receive"), is(true));

        // Verify span attributes
        assertThat("Should have messaging.system attribute",
                consumerSpan.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("messaging.system")),
                is(equalTo("jms")));
    }

    @Test
    void testJmsTracingEndToEnd() {
        spanExporter.reset();

        // Send a message
        String body = "test-message-e2e-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        // Receive the message
        given()
                .when()
                .get("/artemis")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for all spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> spanExporter.getFinishedSpanItems().size() >= 2);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();

        // Verify we have both producer and consumer spans
        long producerSpans = spans.stream()
                .filter(span -> span.getKind() == SpanKind.PRODUCER)
                .count();
        long consumerSpans = spans.stream()
                .filter(span -> span.getKind() == SpanKind.CONSUMER)
                .count();

        assertThat("Should have at least one producer span", producerSpans, is(greaterThan(0L)));
        assertThat("Should have at least one consumer span", consumerSpans, is(greaterThan(0L)));
    }
}
