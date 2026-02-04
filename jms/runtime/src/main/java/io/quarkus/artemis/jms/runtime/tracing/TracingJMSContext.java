package io.quarkus.artemis.jms.runtime.tracing;

import java.io.Serializable;

import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for JMSContext that adds OpenTelemetry tracing.
 */
class TracingJMSContext implements JMSContext {

    protected final JMSContext delegate;
    protected final Tracer tracer;
    protected final JmsContextPropagator contextPropagator;

    TracingJMSContext(JMSContext delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public JMSProducer createProducer() {
        JMSProducer producer = delegate.createProducer();
        return new TracingJMSProducer(producer, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        JMSConsumer consumer = delegate.createConsumer(destination);
        return new TracingJMSConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        JMSConsumer consumer = delegate.createConsumer(destination, messageSelector);
        return new TracingJMSConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        JMSConsumer consumer = delegate.createConsumer(destination, messageSelector, noLocal);
        return new TracingJMSConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        JMSConsumer consumer = delegate.createSharedConsumer(topic, sharedSubscriptionName);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        JMSConsumer consumer = delegate.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        JMSConsumer consumer = delegate.createDurableConsumer(topic, name);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        JMSConsumer consumer = delegate.createDurableConsumer(topic, name, messageSelector, noLocal);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        JMSConsumer consumer = delegate.createSharedDurableConsumer(topic, name);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        JMSConsumer consumer = delegate.createSharedDurableConsumer(topic, name, messageSelector);
        return new TracingJMSConsumer(consumer, topic, tracer, contextPropagator);
    }

    // Delegate all other methods without wrapping

    @Override
    public JMSContext createContext(int sessionMode) {
        return new TracingJMSContext(delegate.createContext(sessionMode), tracer, contextPropagator);
    }

    @Override
    public BytesMessage createBytesMessage() {
        return delegate.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return delegate.createMapMessage();
    }

    @Override
    public Message createMessage() {
        return delegate.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return delegate.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return delegate.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return delegate.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return delegate.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return delegate.createTextMessage(text);
    }

    @Override
    public boolean getTransacted() {
        return delegate.getTransacted();
    }

    @Override
    public int getSessionMode() {
        return delegate.getSessionMode();
    }

    @Override
    public void commit() {
        delegate.commit();
    }

    @Override
    public void rollback() {
        delegate.rollback();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void recover() {
        delegate.recover();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void acknowledge() {
        delegate.acknowledge();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        delegate.setAutoStart(autoStart);
    }

    @Override
    public boolean getAutoStart() {
        return delegate.getAutoStart();
    }

    @Override
    public void setClientID(String clientID) {
        delegate.setClientID(clientID);
    }

    @Override
    public String getClientID() {
        return delegate.getClientID();
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return delegate.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return delegate.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        delegate.setExceptionListener(listener);
    }

    @Override
    public Queue createQueue(String queueName) {
        return delegate.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return delegate.createTopic(topicName);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return delegate.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return delegate.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return delegate.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return delegate.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        delegate.unsubscribe(name);
    }
}
