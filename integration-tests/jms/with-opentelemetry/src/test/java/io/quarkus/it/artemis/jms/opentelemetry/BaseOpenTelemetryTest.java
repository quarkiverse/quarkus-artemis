package io.quarkus.it.artemis.jms.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract public class BaseOpenTelemetryTest {

    private boolean isSpanExporterAvailable;

    @BeforeEach
    void setUp() {
        // Reset spans through REST API
        given().delete("/artemis/spans").then().statusCode(204);

        // Check if span exporter is available (it won't be in native tests)
        List<ArtemisEndpoint.SpanInfo> testSpans = getSpans();
        isSpanExporterAvailable = testSpans != null;

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
        // Skip span verification if exporter is not available (native tests)
        assumeTrue(isSpanExporterAvailable, "Span exporter not available - skipping span verification");

        // Send a message
        String body = "test-message-producer-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        // Wait for spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> getSpans().size() > 0);

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        // Find JMS producer spans
        List<ArtemisEndpoint.SpanInfo> producerSpans = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .toList();

        assertThat("Should have at least one JMS producer span", producerSpans, hasSize(greaterThan(0)));

        ArtemisEndpoint.SpanInfo producerSpan = producerSpans.get(0);
        assertThat("Producer span name should contain 'publish'", producerSpan.name.contains("publish"), is(true));

        // Verify span attributes
        assertThat("Should have messaging.system attribute",
                producerSpan.attributes.get("messaging.system"),
                is(equalTo("jms")));
        assertThat("Should have messaging.destination.name attribute",
                producerSpan.attributes.get("messaging.destination.name"),
                is(notNullValue()));
    }

    @Test
    void testJmsTracingConsumer() {
        // Skip span verification if exporter is not available (native tests)
        assumeTrue(isSpanExporterAvailable, "Span exporter not available - skipping span verification");

        // Send a message first
        String body = "test-message-consumer-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        // Reset spans to focus on consumer
        given().delete("/artemis/spans").then().statusCode(204);

        // Receive the message
        given()
                .when()
                .get("/artemis")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> getSpans().size() > 0);

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        // Find JMS consumer spans
        List<ArtemisEndpoint.SpanInfo> consumerSpans = spans.stream()
                .filter(span -> "CONSUMER".equals(span.kind))
                .toList();

        assertThat("Should have at least one JMS consumer span", consumerSpans, hasSize(greaterThan(0)));

        ArtemisEndpoint.SpanInfo consumerSpan = consumerSpans.get(0);
        assertThat("Consumer span name should contain 'receive'", consumerSpan.name.contains("receive"), is(true));

        // Verify span attributes
        assertThat("Should have messaging.system attribute",
                consumerSpan.attributes.get("messaging.system"),
                is(equalTo("jms")));
    }

    @Test
    void testJmsTracingEndToEnd() {
        // Skip span verification if exporter is not available (native tests)
        assumeTrue(isSpanExporterAvailable, "Span exporter not available - skipping span verification");

        // Reset spans
        given().delete("/artemis/spans").then().statusCode(204);

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
        await().atMost(10, TimeUnit.SECONDS).until(() -> getSpans().size() >= 2);

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        // Verify we have both producer and consumer spans
        long producerSpans = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .count();
        long consumerSpans = spans.stream()
                .filter(span -> "CONSUMER".equals(span.kind))
                .count();

        assertThat("Should have at least one producer span", producerSpans, is(greaterThan(0L)));
        assertThat("Should have at least one consumer span", consumerSpans, is(greaterThan(0L)));
    }

    @Test
    void testBasicJmsOperations() {
        // This test runs in both JVM and native mode - just tests basic JMS functionality
        String body = "test-message-basic-" + System.currentTimeMillis();

        // Send a message
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
    }

    private List<ArtemisEndpoint.SpanInfo> getSpans() {
        return given()
                .when()
                .get("/artemis/spans")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", ArtemisEndpoint.SpanInfo.class);
    }
}
