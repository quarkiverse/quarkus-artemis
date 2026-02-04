package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.JMSContext;
import jakarta.jms.Session;
import jakarta.jms.XAJMSContext;

import javax.transaction.xa.XAResource;

import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for XAJMSContext that adds OpenTelemetry tracing.
 */
class TracingXAJMSContext extends TracingJMSContext implements XAJMSContext {

    private final XAJMSContext delegate;

    TracingXAJMSContext(XAJMSContext delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        super(delegate, tracer, contextPropagator);
        this.delegate = delegate;
    }

    @Override
    public JMSContext getContext() {
        return new TracingJMSContext(delegate.getContext(), tracer, contextPropagator);
    }

    @Override
    public Session getSession() {
        return new TracingSession(delegate.getSession(), tracer, contextPropagator);
    }

    @Override
    public XAResource getXAResource() {
        return delegate.getXAResource();
    }
}
