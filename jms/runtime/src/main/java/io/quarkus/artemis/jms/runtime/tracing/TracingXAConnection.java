package io.quarkus.artemis.jms.runtime.tracing;

import javax.transaction.xa.XAResource;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.XAConnection;
import jakarta.jms.XASession;

import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for XAConnection that adds OpenTelemetry tracing.
 */
class TracingXAConnection extends TracingConnection implements XAConnection {

    private final XAConnection delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingXAConnection(XAConnection delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        super(delegate, tracer, contextPropagator);
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public XASession createXASession() throws JMSException {
        XASession session = delegate.createXASession();
        return new TracingXASession(session, tracer, contextPropagator);
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return new TracingSession(delegate.createSession(transacted, acknowledgeMode), tracer, contextPropagator);
    }

    /**
     * Wrapper for XASession that adds tracing.
     */
    private static class TracingXASession extends TracingSession implements XASession {
        private final XASession delegate;

        TracingXASession(XASession delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
            super(delegate, tracer, contextPropagator);
            this.delegate = delegate;
        }

        @Override
        public XAResource getXAResource() {
            return delegate.getXAResource();
        }

        @Override
        public Session getSession() throws JMSException {
            return delegate.getSession();
        }

        @Override
        public boolean getTransacted() throws JMSException {
            return delegate.getTransacted();
        }

        @Override
        public void commit() throws JMSException {
            delegate.commit();
        }

        @Override
        public void rollback() throws JMSException {
            delegate.rollback();
        }
    }
}
