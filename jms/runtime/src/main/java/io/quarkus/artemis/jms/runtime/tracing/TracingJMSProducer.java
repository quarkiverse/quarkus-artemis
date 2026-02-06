package io.quarkus.artemis.jms.runtime.tracing;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import jakarta.jms.CompletionListener;
import jakarta.jms.Destination;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/**
 * Wrapper for JMSProducer that adds OpenTelemetry tracing.
 */
class TracingJMSProducer implements JMSProducer {

    private final JMSProducer delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingJMSProducer(JMSProducer delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public JMSProducer send(Destination destination, Message message) {
        sendWithTracing(destination, message);
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, String body) {
        sendConvenienceWithTracing(destination, () -> delegate.send(destination, body));
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, Map<String, Object> body) {
        sendConvenienceWithTracing(destination, () -> delegate.send(destination, body));
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, byte[] body) {
        sendConvenienceWithTracing(destination, () -> delegate.send(destination, body));
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, Serializable body) {
        sendConvenienceWithTracing(destination, () -> delegate.send(destination, body));
        return this;
    }

    /**
     * Traces a convenience send operation (String, Map, byte[], Serializable).
     * These methods don't provide a Message object, so trace context is injected
     * via JMSProducer properties which are applied to the internally-created message.
     */
    private void sendConvenienceWithTracing(Destination destination, Runnable sendOperation) {
        String spanName = JmsSpanAttributes.generateSpanName(destination, "publish");
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.PRODUCER);

        Span span = spanBuilder.startSpan();
        try (Scope scope = span.makeCurrent()) {
            JmsSpanAttributes.setSpanAttributes(span, destination, null);
            contextPropagator.injectContext(Context.current(), delegate);
            sendOperation.run();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private void sendWithTracing(Destination destination, Message message) {
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
            delegate.send(destination, message);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    // Delegate all other methods

    @Override
    public JMSProducer setDisableMessageID(boolean value) {
        delegate.setDisableMessageID(value);
        return this;
    }

    @Override
    public boolean getDisableMessageID() {
        return delegate.getDisableMessageID();
    }

    @Override
    public JMSProducer setDisableMessageTimestamp(boolean value) {
        delegate.setDisableMessageTimestamp(value);
        return this;
    }

    @Override
    public boolean getDisableMessageTimestamp() {
        return delegate.getDisableMessageTimestamp();
    }

    @Override
    public JMSProducer setDeliveryMode(int deliveryMode) {
        delegate.setDeliveryMode(deliveryMode);
        return this;
    }

    @Override
    public int getDeliveryMode() {
        return delegate.getDeliveryMode();
    }

    @Override
    public JMSProducer setPriority(int priority) {
        delegate.setPriority(priority);
        return this;
    }

    @Override
    public int getPriority() {
        return delegate.getPriority();
    }

    @Override
    public JMSProducer setTimeToLive(long timeToLive) {
        delegate.setTimeToLive(timeToLive);
        return this;
    }

    @Override
    public long getTimeToLive() {
        return delegate.getTimeToLive();
    }

    @Override
    public JMSProducer setDeliveryDelay(long deliveryDelay) {
        delegate.setDeliveryDelay(deliveryDelay);
        return this;
    }

    @Override
    public long getDeliveryDelay() {
        return delegate.getDeliveryDelay();
    }

    @Override
    public JMSProducer setAsync(CompletionListener completionListener) {
        delegate.setAsync(completionListener);
        return this;
    }

    @Override
    public CompletionListener getAsync() {
        return delegate.getAsync();
    }

    @Override
    public JMSProducer setProperty(String name, boolean value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, byte value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, short value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, int value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, long value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, float value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, double value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, String value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer setProperty(String name, Object value) {
        delegate.setProperty(name, value);
        return this;
    }

    @Override
    public JMSProducer clearProperties() {
        delegate.clearProperties();
        return this;
    }

    @Override
    public boolean propertyExists(String name) {
        return delegate.propertyExists(name);
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return delegate.getBooleanProperty(name);
    }

    @Override
    public byte getByteProperty(String name) {
        return delegate.getByteProperty(name);
    }

    @Override
    public short getShortProperty(String name) {
        return delegate.getShortProperty(name);
    }

    @Override
    public int getIntProperty(String name) {
        return delegate.getIntProperty(name);
    }

    @Override
    public long getLongProperty(String name) {
        return delegate.getLongProperty(name);
    }

    @Override
    public float getFloatProperty(String name) {
        return delegate.getFloatProperty(name);
    }

    @Override
    public double getDoubleProperty(String name) {
        return delegate.getDoubleProperty(name);
    }

    @Override
    public String getStringProperty(String name) {
        return delegate.getStringProperty(name);
    }

    @Override
    public Object getObjectProperty(String name) {
        return delegate.getObjectProperty(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
        delegate.setJMSCorrelationIDAsBytes(correlationID);
        return this;
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return delegate.getJMSCorrelationIDAsBytes();
    }

    @Override
    public JMSProducer setJMSCorrelationID(String correlationID) {
        delegate.setJMSCorrelationID(correlationID);
        return this;
    }

    @Override
    public String getJMSCorrelationID() {
        return delegate.getJMSCorrelationID();
    }

    @Override
    public JMSProducer setJMSType(String type) {
        delegate.setJMSType(type);
        return this;
    }

    @Override
    public String getJMSType() {
        return delegate.getJMSType();
    }

    @Override
    public JMSProducer setJMSReplyTo(Destination replyTo) {
        delegate.setJMSReplyTo(replyTo);
        return this;
    }

    @Override
    public Destination getJMSReplyTo() {
        return delegate.getJMSReplyTo();
    }
}
