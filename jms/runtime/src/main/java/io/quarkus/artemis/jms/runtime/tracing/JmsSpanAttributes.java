package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;

/**
 * Helper class for setting OpenTelemetry semantic convention attributes on JMS spans.
 * Based on OpenTelemetry Semantic Conventions for Messaging:
 * https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/
 */
class JmsSpanAttributes {

    private static final String MESSAGING_SYSTEM = "jms";

    // Semantic convention attribute keys
    private static final AttributeKey<String> MESSAGING_SYSTEM_KEY = AttributeKey.stringKey("messaging.system");
    private static final AttributeKey<String> MESSAGING_DESTINATION_NAME_KEY = AttributeKey
            .stringKey("messaging.destination.name");
    // Note: messaging.destination.kind is not officially defined in the OpenTelemetry semantic conventions yet.
    // This attribute may be subject to change if/when it's officially standardized.
    private static final AttributeKey<String> MESSAGING_DESTINATION_KIND_KEY = AttributeKey
            .stringKey("messaging.destination.kind");
    private static final AttributeKey<String> MESSAGING_MESSAGE_ID_KEY = AttributeKey.stringKey("messaging.message.id");
    private static final AttributeKey<String> MESSAGING_CONVERSATION_ID_KEY = AttributeKey
            .stringKey("messaging.message.conversation_id");

    /**
     * Sets span attributes for a JMS message operation.
     *
     * @param span the span to set attributes on
     * @param destination the JMS destination
     * @param message the JMS message (optional)
     */
    static void setSpanAttributes(Span span, Destination destination, Message message) {
        if (span == null) {
            return;
        }

        // Set messaging system
        span.setAttribute(MESSAGING_SYSTEM_KEY, MESSAGING_SYSTEM);

        // Set destination information
        if (destination != null) {
            setDestinationAttributes(span, destination);
        }

        // Set message attributes
        if (message != null) {
            setMessageAttributes(span, message);
        }
    }

    private static void setDestinationAttributes(Span span, Destination destination) {
        try {
            String destinationName = getDestinationName(destination);
            if (destinationName != null) {
                span.setAttribute(MESSAGING_DESTINATION_NAME_KEY, destinationName);
            }

            // Set destination kind (queue or topic)
            if (destination instanceof Queue) {
                span.setAttribute(MESSAGING_DESTINATION_KIND_KEY, "queue");
            } else if (destination instanceof Topic) {
                span.setAttribute(MESSAGING_DESTINATION_KIND_KEY, "topic");
            }
        } catch (Exception e) {
            // Best effort - ignore errors
        }
    }

    private static void setMessageAttributes(Span span, Message message) {
        try {
            // Message ID
            String messageId = message.getJMSMessageID();
            if (messageId != null) {
                span.setAttribute(MESSAGING_MESSAGE_ID_KEY, messageId);
            }

            // Correlation ID (conversation ID in semconv)
            String correlationId = message.getJMSCorrelationID();
            if (correlationId != null) {
                span.setAttribute(MESSAGING_CONVERSATION_ID_KEY, correlationId);
            }
        } catch (JMSException e) {
            // Best effort - ignore errors
        }
    }

    private static String getDestinationName(Destination destination) {
        try {
            if (destination instanceof Queue) {
                return ((Queue) destination).getQueueName();
            } else if (destination instanceof Topic) {
                return ((Topic) destination).getTopicName();
            }
        } catch (JMSException e) {
            // Ignore
        }
        return null;
    }

    /**
     * Generates a span name for a JMS operation.
     *
     * @param destination the JMS destination
     * @param operation the operation name (e.g., "publish", "receive")
     * @return the span name
     */
    static String generateSpanName(Destination destination, String operation) {
        String destinationName = getDestinationName(destination);
        if (destinationName != null) {
            return destinationName + " " + operation;
        }
        return "jms " + operation;
    }
}
