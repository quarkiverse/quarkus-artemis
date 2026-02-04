package io.quarkus.artemis.jms.runtime.tracing;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XAJMSContext;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for ConnectionFactory that adds OpenTelemetry tracing to all created connections.
 */
public class TracingConnectionFactory implements ConnectionFactory, XAConnectionFactory {

    private final ConnectionFactory delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    public TracingConnectionFactory(ConnectionFactory delegate, OpenTelemetry openTelemetry) {
        this.delegate = delegate;
        this.tracer = openTelemetry.getTracer("io.quarkus.artemis.jms");
        this.contextPropagator = new JmsContextPropagator(openTelemetry.getPropagators());
    }

    @Override
    public Connection createConnection() throws JMSException {
        Connection connection = delegate.createConnection();
        return new TracingConnection(connection, tracer, contextPropagator);
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        Connection connection = delegate.createConnection(userName, password);
        return new TracingConnection(connection, tracer, contextPropagator);
    }

    @Override
    public JMSContext createContext() {
        JMSContext context = delegate.createContext();
        return new TracingJMSContext(context, tracer, contextPropagator);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        JMSContext context = delegate.createContext(userName, password);
        return new TracingJMSContext(context, tracer, contextPropagator);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        JMSContext context = delegate.createContext(userName, password, sessionMode);
        return new TracingJMSContext(context, tracer, contextPropagator);
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        JMSContext context = delegate.createContext(sessionMode);
        return new TracingJMSContext(context, tracer, contextPropagator);
    }

    // XAConnectionFactory methods (if delegate implements it)

    @Override
    public XAConnection createXAConnection() throws JMSException {
        if (delegate instanceof XAConnectionFactory) {
            XAConnection connection = ((XAConnectionFactory) delegate).createXAConnection();
            return new TracingXAConnection(connection, tracer, contextPropagator);
        }
        throw new JMSException("Delegate does not support XA connections");
    }

    @Override
    public XAConnection createXAConnection(String userName, String password) throws JMSException {
        if (delegate instanceof XAConnectionFactory) {
            XAConnection connection = ((XAConnectionFactory) delegate).createXAConnection(userName, password);
            return new TracingXAConnection(connection, tracer, contextPropagator);
        }
        throw new JMSException("Delegate does not support XA connections");
    }

    @Override
    public XAJMSContext createXAContext() {
        if (delegate instanceof XAConnectionFactory) {
            XAJMSContext context = ((XAConnectionFactory) delegate).createXAContext();
            return new TracingXAJMSContext(context, tracer, contextPropagator);
        }
        throw new UnsupportedOperationException("Delegate does not support XA contexts");
    }

    @Override
    public XAJMSContext createXAContext(String userName, String password) {
        if (delegate instanceof XAConnectionFactory) {
            XAJMSContext context = ((XAConnectionFactory) delegate).createXAContext(userName, password);
            return new TracingXAJMSContext(context, tracer, contextPropagator);
        }
        throw new UnsupportedOperationException("Delegate does not support XA contexts");
    }
}
