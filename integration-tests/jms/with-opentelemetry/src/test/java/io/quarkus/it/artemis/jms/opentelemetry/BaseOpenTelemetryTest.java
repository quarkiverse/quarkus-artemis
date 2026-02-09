package io.quarkus.it.artemis.jms.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract public class BaseOpenTelemetryTest {

    private static final int MAX_DRAIN_ITERATIONS = 50;

    @BeforeEach
    void setUp() {
        // Reset spans through REST API
        given().delete("/artemis/spans").then().statusCode(204);

        // Clear any leftover messages from the queue with a bounded loop
        int iterations = 0;
        try {
            String leftover;
            while (iterations++ < MAX_DRAIN_ITERATIONS
                    && (leftover = given().when().get("/artemis").then().extract().asString()) != null
                    && !leftover.isEmpty()) {
                // Consume leftover messages
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

        // Wait for JMS producer span to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            long jmsProducerCount = getSpans().stream().filter(span -> "PRODUCER".equals(span.kind)).count();
            return jmsProducerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        List<ArtemisEndpoint.SpanInfo> producerSpans = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .toList();

        assertThat("Should have at least one JMS producer span", producerSpans, hasSize(greaterThanOrEqualTo(1)));

        ArtemisEndpoint.SpanInfo producerSpan = producerSpans.get(0);
        assertThat("Producer span name should contain 'publish'", producerSpan.name, containsString("publish"));

        // Verify span attributes
        assertThat("Should have messaging.system=jms",
                producerSpan.attributes.get("messaging.system"), is(equalTo("jms")));
        assertThat("Should have messaging.destination.name=test-jms-otel",
                producerSpan.attributes.get("messaging.destination.name"), is(equalTo("test-jms-otel")));
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

        // Reset spans to focus on consumer
        given().delete("/artemis/spans").then().statusCode(204);

        // Receive the message
        given()
                .when()
                .get("/artemis")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for JMS consumer span to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            long jmsConsumerCount = getSpans().stream().filter(span -> "CONSUMER".equals(span.kind)).count();
            return jmsConsumerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        List<ArtemisEndpoint.SpanInfo> consumerSpans = spans.stream()
                .filter(span -> "CONSUMER".equals(span.kind))
                .toList();

        assertThat("Should have at least one JMS consumer span", consumerSpans, hasSize(greaterThanOrEqualTo(1)));

        ArtemisEndpoint.SpanInfo consumerSpan = consumerSpans.get(0);
        assertThat("Consumer span name should contain 'receive'", consumerSpan.name, containsString("receive"));

        // Verify span attributes
        assertThat("Should have messaging.system=jms",
                consumerSpan.attributes.get("messaging.system"), is(equalTo("jms")));
        assertThat("Should have messaging.destination.name=test-jms-otel",
                consumerSpan.attributes.get("messaging.destination.name"), is(equalTo("test-jms-otel")));
    }

    @Test
    void testJmsTracingContextPropagation() {
        // Reset spans
        given().delete("/artemis/spans").then().statusCode(204);

        // Use the JMS 2.0 convenience API (JMSContext/JMSProducer.send(Destination, String))
        // This verifies context propagation via JMSProducer properties
        String body = "test-message-propagation-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis")
                .then()
                .statusCode(204);

        // Receive via JMS 2.0 API
        given()
                .when()
                .get("/artemis")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for both JMS producer and consumer spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            List<ArtemisEndpoint.SpanInfo> allSpans = getSpans();
            long producerCount = allSpans.stream().filter(span -> "PRODUCER".equals(span.kind)).count();
            long consumerCount = allSpans.stream().filter(span -> "CONSUMER".equals(span.kind)).count();
            return producerCount >= 1 && consumerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        ArtemisEndpoint.SpanInfo producerSpan = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No producer span found"));

        ArtemisEndpoint.SpanInfo consumerSpan = spans.stream()
                .filter(span -> "CONSUMER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No consumer span found"));

        // Verify the consumer span has a link back to the producer span's context
        assertThat("Consumer span should have at least one link for trace context propagation",
                consumerSpan.links, is(not(empty())));

        boolean hasLinkToProducer = consumerSpan.links.stream()
                .anyMatch(link -> link.traceId.equals(producerSpan.traceId)
                        && link.spanId.equals(producerSpan.spanId));
        assertThat("Consumer span should link to the producer span", hasLinkToProducer, is(true));
    }

    @Test
    void testJmsTracingClassicApi() {
        // Reset spans
        given().delete("/artemis/spans").then().statusCode(204);

        // Send a message using classic Connection/Session API
        String body = "test-message-classic-" + System.currentTimeMillis();
        given()
                .body(body)
                .when()
                .post("/artemis/classic")
                .then()
                .statusCode(204);

        // Receive the message using classic Connection/Session API
        given()
                .when()
                .get("/artemis/classic")
                .then()
                .statusCode(200)
                .body(equalTo(body));

        // Wait for both JMS producer and consumer spans to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            List<ArtemisEndpoint.SpanInfo> allSpans = getSpans();
            long producerCount = allSpans.stream().filter(span -> "PRODUCER".equals(span.kind)).count();
            long consumerCount = allSpans.stream().filter(span -> "CONSUMER".equals(span.kind)).count();
            return producerCount >= 1 && consumerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        ArtemisEndpoint.SpanInfo producerSpan = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No producer span found for classic API"));

        ArtemisEndpoint.SpanInfo consumerSpan = spans.stream()
                .filter(span -> "CONSUMER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No consumer span found for classic API"));

        // Verify producer span
        assertThat("Producer span name should contain 'publish'", producerSpan.name, containsString("publish"));
        assertThat("Producer should have messaging.system=jms",
                producerSpan.attributes.get("messaging.system"), is(equalTo("jms")));
        assertThat("Producer should have messaging.destination.name=test-jms-otel",
                producerSpan.attributes.get("messaging.destination.name"), is(equalTo("test-jms-otel")));

        // Verify consumer span
        assertThat("Consumer span name should contain 'receive'", consumerSpan.name, containsString("receive"));
        assertThat("Consumer should have messaging.system=jms",
                consumerSpan.attributes.get("messaging.system"), is(equalTo("jms")));
        assertThat("Consumer should have messaging.destination.name=test-jms-otel",
                consumerSpan.attributes.get("messaging.destination.name"), is(equalTo("test-jms-otel")));

        // Verify context propagation through the classic API path
        assertThat("Consumer span should have links for trace context propagation",
                consumerSpan.links, is(not(empty())));

        boolean hasLinkToProducer = consumerSpan.links.stream()
                .anyMatch(link -> link.traceId.equals(producerSpan.traceId)
                        && link.spanId.equals(producerSpan.spanId));
        assertThat("Consumer span should link to the producer span", hasLinkToProducer, is(true));
    }

    @Test
    void testJmsTracingProducerErrorClassicApi() {
        // Reset spans
        given().delete("/artemis/spans").then().statusCode(204);

        // Trigger a send error via the classic API (sends on a closed session)
        given()
                .body("test-error-classic")
                .when()
                .post("/artemis/error/classic")
                .then()
                .statusCode(500);

        // Wait for a producer span to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            long producerCount = getSpans().stream().filter(span -> "PRODUCER".equals(span.kind)).count();
            return producerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        ArtemisEndpoint.SpanInfo errorSpan = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No producer span found for error case"));

        // Verify the span is marked as ERROR
        assertThat("Error span should have ERROR status", errorSpan.statusCode, is(equalTo("ERROR")));
        assertThat("Error span status should have a description",
                errorSpan.statusDescription, is(not(emptyOrNullString())));

        // Verify the exception event was recorded
        assertThat("Error span should have events", errorSpan.events, is(not(empty())));
        boolean hasExceptionEvent = errorSpan.events.stream()
                .anyMatch(event -> "exception".equals(event.name));
        assertThat("Error span should have an 'exception' event", hasExceptionEvent, is(true));

        // Verify exception event attributes contain the exception type and message
        ArtemisEndpoint.EventInfo exceptionEvent = errorSpan.events.stream()
                .filter(event -> "exception".equals(event.name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No exception event found"));
        assertThat("Exception event should have exception.type",
                exceptionEvent.attributes.get("exception.type"), is(notNullValue()));
        assertThat("Exception event should have exception.message",
                exceptionEvent.attributes.get("exception.message"), is(notNullValue()));
    }

    @Test
    void testJmsTracingProducerErrorJms2Api() {
        // Reset spans
        given().delete("/artemis/spans").then().statusCode(204);

        // Trigger a send error via JMS 2.0 API (sends a null message)
        given()
                .when()
                .post("/artemis/error/jms2")
                .then()
                .statusCode(500);

        // Wait for a producer span to be exported
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            long producerCount = getSpans().stream().filter(span -> "PRODUCER".equals(span.kind)).count();
            return producerCount >= 1;
        });

        List<ArtemisEndpoint.SpanInfo> spans = getSpans();

        ArtemisEndpoint.SpanInfo errorSpan = spans.stream()
                .filter(span -> "PRODUCER".equals(span.kind))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No producer span found for JMS 2.0 error case"));

        // Verify the span is marked as ERROR
        assertThat("Error span should have ERROR status", errorSpan.statusCode, is(equalTo("ERROR")));

        // Verify the exception event was recorded
        boolean hasExceptionEvent = errorSpan.events.stream()
                .anyMatch(event -> "exception".equals(event.name));
        assertThat("Error span should have an 'exception' event", hasExceptionEvent, is(true));

        // Verify the original exception propagated (endpoint returned 500)
        // and the span captured the exception details
        ArtemisEndpoint.EventInfo exceptionEvent = errorSpan.events.stream()
                .filter(event -> "exception".equals(event.name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No exception event found"));
        assertThat("Exception event should have exception.type",
                exceptionEvent.attributes.get("exception.type"), is(notNullValue()));
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
