package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.CompletionListener;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/**
 * Wrapper for MessageProducer that adds OpenTelemetry tracing.
 */
class TracingMessageProducer implements MessageProducer {

    private final MessageProducer delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingMessageProducer(MessageProducer delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public void send(Message message) throws JMSException {
        Destination destination = getDestination();
        if (destination == null) {
            destination = message.getJMSDestination();
        }
        sendWithTracing(destination, message, () -> delegate.send(message));
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        Destination destination = getDestination();
        if (destination == null) {
            destination = message.getJMSDestination();
        }
        sendWithTracing(destination, message, () -> delegate.send(message, deliveryMode, priority, timeToLive));
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        sendWithTracing(destination, message, () -> delegate.send(destination, message));
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive)
            throws JMSException {
        sendWithTracing(destination, message,
                () -> delegate.send(destination, message, deliveryMode, priority, timeToLive));
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {
        Destination destination = getDestination();
        if (destination == null) {
            destination = message.getJMSDestination();
        }
        sendWithTracing(destination, message, () -> delegate.send(message, wrapCompletionListener(completionListener)));
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener)
            throws JMSException {
        Destination destination = getDestination();
        if (destination == null) {
            destination = message.getJMSDestination();
        }
        sendWithTracing(destination, message,
                () -> delegate.send(message, deliveryMode, priority, timeToLive, wrapCompletionListener(completionListener)));
    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {
        sendWithTracing(destination, message,
                () -> delegate.send(destination, message, wrapCompletionListener(completionListener)));
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive,
            CompletionListener completionListener) throws JMSException {
        sendWithTracing(destination, message,
                () -> delegate.send(destination, message, deliveryMode, priority, timeToLive,
                        wrapCompletionListener(completionListener)));
    }

    private void sendWithTracing(Destination destination, Message message, JmsOperation operation) throws JMSException {
        String spanName = JmsSpanAttributes.generateSpanName(destination, "publish");
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.PRODUCER);

        Span span = spanBuilder.startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Inject context into message
            contextPropagator.injectContext(Context.current(), message);

            // Set span attributes
            JmsSpanAttributes.setSpanAttributes(span, destination, message);

            // Execute the send operation
            operation.execute();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            if (e instanceof JMSException) {
                throw (JMSException) e;
            } else {
                throw new JMSException(e.getMessage());
            }
        } finally {
            span.end();
        }
    }

    private CompletionListener wrapCompletionListener(CompletionListener listener) {
        if (listener == null) {
            return null;
        }
        return new CompletionListener() {
            @Override
            public void onCompletion(Message message) {
                listener.onCompletion(message);
            }

            @Override
            public void onException(Message message, Exception exception) {
                listener.onException(message, exception);
            }
        };
    }

    @FunctionalInterface
    private interface JmsOperation {
        void execute() throws JMSException;
    }

    // Delegate all other methods

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        delegate.setDisableMessageID(value);
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return delegate.getDisableMessageID();
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        delegate.setDisableMessageTimestamp(value);
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return delegate.getDisableMessageTimestamp();
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        delegate.setDeliveryMode(deliveryMode);
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return delegate.getDeliveryMode();
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        delegate.setPriority(defaultPriority);
    }

    @Override
    public int getPriority() throws JMSException {
        return delegate.getPriority();
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        delegate.setTimeToLive(timeToLive);
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return delegate.getTimeToLive();
    }

    @Override
    public void setDeliveryDelay(long deliveryDelay) throws JMSException {
        delegate.setDeliveryDelay(deliveryDelay);
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return delegate.getDeliveryDelay();
    }

    @Override
    public Destination getDestination() throws JMSException {
        return delegate.getDestination();
    }

    @Override
    public void close() throws JMSException {
        delegate.close();
    }
}
