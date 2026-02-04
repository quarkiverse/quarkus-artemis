package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for Connection that adds OpenTelemetry tracing to created sessions.
 */
class TracingConnection implements Connection {

    private final Connection delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingConnection(Connection delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        Session session = delegate.createSession(transacted, acknowledgeMode);
        return new TracingSession(session, tracer, contextPropagator);
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        Session session = delegate.createSession(sessionMode);
        return new TracingSession(session, tracer, contextPropagator);
    }

    @Override
    public Session createSession() throws JMSException {
        Session session = delegate.createSession();
        return new TracingSession(session, tracer, contextPropagator);
    }

    // Delegate all other methods without wrapping

    @Override
    public String getClientID() throws JMSException {
        return delegate.getClientID();
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        delegate.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return delegate.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return delegate.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        delegate.setExceptionListener(listener);
    }

    @Override
    public void start() throws JMSException {
        delegate.start();
    }

    @Override
    public void stop() throws JMSException {
        delegate.stop();
    }

    @Override
    public void close() throws JMSException {
        delegate.close();
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return delegate.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return delegate.createSharedConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return delegate.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return delegate.createSharedDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool,
                maxMessages);
    }
}
