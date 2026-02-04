package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/**
 * Wrapper for JMSConsumer that adds OpenTelemetry tracing.
 */
class TracingJMSConsumer implements JMSConsumer {

    private final JMSConsumer delegate;
    private final Destination destination;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingJMSConsumer(JMSConsumer delegate, Destination destination, Tracer tracer,
            JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.destination = destination;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public Message receive() {
        Message message = delegate.receive();
        if (message != null) {
            createReceiveSpan(message);
        }
        return message;
    }

    @Override
    public Message receive(long timeout) {
        Message message = delegate.receive(timeout);
        if (message != null) {
            createReceiveSpan(message);
        }
        return message;
    }

    @Override
    public Message receiveNoWait() {
        Message message = delegate.receiveNoWait();
        if (message != null) {
            createReceiveSpan(message);
        }
        return message;
    }

    @Override
    public <T> T receiveBody(Class<T> c) {
        // Note: We can't access the message here, so no tracing for receiveBody
        return delegate.receiveBody(c);
    }

    @Override
    public <T> T receiveBody(Class<T> c, long timeout) {
        return delegate.receiveBody(c, timeout);
    }

    @Override
    public <T> T receiveBodyNoWait(Class<T> c) {
        return delegate.receiveBodyNoWait(c);
    }

    private void createReceiveSpan(Message message) {
        String spanName = JmsSpanAttributes.generateSpanName(destination, "receive");

        // Extract context from message to create a link
        Context extractedContext = contextPropagator.extractContext(message);

        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.CONSUMER);

        // Use link instead of parent-child relationship as per OTel messaging spec
        if (extractedContext != null) {
            Span parentSpan = Span.fromContext(extractedContext);
            if (parentSpan != null && parentSpan.getSpanContext().isValid()) {
                spanBuilder.addLink(parentSpan.getSpanContext());
            }
        }

        Span span = spanBuilder.startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Set span attributes
            JmsSpanAttributes.setSpanAttributes(span, destination, message);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
        } finally {
            span.end();
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        if (listener == null) {
            delegate.setMessageListener(null);
        } else {
            delegate.setMessageListener(new TracingMessageListener(listener, destination, tracer, contextPropagator));
        }
    }

    @Override
    public MessageListener getMessageListener() {
        return delegate.getMessageListener();
    }

    @Override
    public String getMessageSelector() {
        return delegate.getMessageSelector();
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * Wrapper for MessageListener that adds tracing.
     */
    private static class TracingMessageListener implements MessageListener {
        private final MessageListener delegate;
        private final Destination destination;
        private final Tracer tracer;
        private final JmsContextPropagator contextPropagator;

        TracingMessageListener(MessageListener delegate, Destination destination, Tracer tracer,
                JmsContextPropagator contextPropagator) {
            this.delegate = delegate;
            this.destination = destination;
            this.tracer = tracer;
            this.contextPropagator = contextPropagator;
        }

        @Override
        public void onMessage(Message message) {
            String spanName = JmsSpanAttributes.generateSpanName(destination, "receive");

            // Extract context from message to create a link
            Context extractedContext = contextPropagator.extractContext(message);

            SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.CONSUMER);

            // Use link instead of parent-child relationship
            if (extractedContext != null) {
                Span parentSpan = Span.fromContext(extractedContext);
                if (parentSpan != null && parentSpan.getSpanContext().isValid()) {
                    spanBuilder.addLink(parentSpan.getSpanContext());
                }
            }

            Span span = spanBuilder.startSpan();
            try (Scope scope = span.makeCurrent()) {
                // Set span attributes
                JmsSpanAttributes.setSpanAttributes(span, destination, message);

                // Call the delegate listener
                delegate.onMessage(message);
            } catch (Exception e) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
                throw e;
            } finally {
                span.end();
            }
        }
    }
}
