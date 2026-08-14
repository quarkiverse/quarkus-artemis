package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/**
 * Wrapper for MessageConsumer that adds OpenTelemetry tracing.
 */
class TracingMessageConsumer implements MessageConsumer {

    private final MessageConsumer delegate;
    private final Destination destination;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingMessageConsumer(MessageConsumer delegate, Destination destination, Tracer tracer,
            JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.destination = destination;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public Message receive() throws JMSException {
        return receiveWithTracing(() -> delegate.receive());
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        return receiveWithTracing(() -> delegate.receive(timeout));
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return receiveWithTracing(() -> delegate.receiveNoWait());
    }

    private Message receiveWithTracing(JmsReceiveOperation operation) throws JMSException {
        Message message = operation.execute();
        if (message != null) {
            createReceiveSpan(message);
        }
        return message;
    }

    private void createReceiveSpan(Message message) {
        String spanName = JmsSpanAttributes.generateSpanName(destination, "receive");

        // Extract context from message to establish parent-child relationship
        Context extractedContext = contextPropagator.extractContext(message);

        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.CONSUMER);

        // Set extracted context as parent so the consumer span shares the same traceId
        if (extractedContext != null) {
            spanBuilder.setParent(extractedContext);
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
    public void setMessageListener(MessageListener listener) throws JMSException {
        if (listener == null) {
            delegate.setMessageListener(null);
        } else {
            delegate.setMessageListener(new TracingMessageListener(listener, destination, tracer, contextPropagator));
        }
    }

    @FunctionalInterface
    private interface JmsReceiveOperation {
        Message execute() throws JMSException;
    }

    // Delegate all other methods

    @Override
    public String getMessageSelector() throws JMSException {
        return delegate.getMessageSelector();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return delegate.getMessageListener();
    }

    @Override
    public void close() throws JMSException {
        delegate.close();
    }

    /**
     * Wrapper for MessageListener that adds tracing to onMessage calls.
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

            // Extract context from message to establish parent-child relationship
            Context extractedContext = contextPropagator.extractContext(message);

            SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.CONSUMER);

            // Set extracted context as parent so the consumer span shares the same traceId
            if (extractedContext != null) {
                spanBuilder.setParent(extractedContext);
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
