package io.quarkus.artemis.jms.runtime.tracing;

import java.io.Serializable;

import jakarta.jms.BytesMessage;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.jms.TopicSubscriber;

import io.opentelemetry.api.trace.Tracer;

/**
 * Wrapper for Session that adds OpenTelemetry tracing to created producers and consumers.
 */
class TracingSession implements Session {

    private final Session delegate;
    private final Tracer tracer;
    private final JmsContextPropagator contextPropagator;

    TracingSession(Session delegate, Tracer tracer, JmsContextPropagator contextPropagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.contextPropagator = contextPropagator;
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        MessageProducer producer = delegate.createProducer(destination);
        return new TracingMessageProducer(producer, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        MessageConsumer consumer = delegate.createConsumer(destination);
        return new TracingMessageConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        MessageConsumer consumer = delegate.createConsumer(destination, messageSelector);
        return new TracingMessageConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal)
            throws JMSException {
        MessageConsumer consumer = delegate.createConsumer(destination, messageSelector, noLocal);
        return new TracingMessageConsumer(consumer, destination, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        MessageConsumer consumer = delegate.createSharedConsumer(topic, sharedSubscriptionName);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector)
            throws JMSException {
        MessageConsumer consumer = delegate.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        MessageConsumer consumer = delegate.createDurableConsumer(topic, name);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        MessageConsumer consumer = delegate.createDurableConsumer(topic, name, messageSelector, noLocal);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        MessageConsumer consumer = delegate.createSharedDurableConsumer(topic, name);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) throws JMSException {
        MessageConsumer consumer = delegate.createSharedDurableConsumer(topic, name, messageSelector);
        return new TracingMessageConsumer(consumer, topic, tracer, contextPropagator);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        TopicSubscriber subscriber = delegate.createDurableSubscriber(topic, name);
        return new TracingTopicSubscriber(subscriber, topic, tracer, contextPropagator);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        TopicSubscriber subscriber = delegate.createDurableSubscriber(topic, name, messageSelector, noLocal);
        return new TracingTopicSubscriber(subscriber, topic, tracer, contextPropagator);
    }

    // Delegate all other methods without wrapping

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        return delegate.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return delegate.createMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        return delegate.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return delegate.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        return delegate.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return delegate.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return delegate.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        return delegate.createTextMessage(text);
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return delegate.getTransacted();
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return delegate.getAcknowledgeMode();
    }

    @Override
    public void commit() throws JMSException {
        delegate.commit();
    }

    @Override
    public void rollback() throws JMSException {
        delegate.rollback();
    }

    @Override
    public void close() throws JMSException {
        delegate.close();
    }

    @Override
    public void recover() throws JMSException {
        delegate.recover();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return delegate.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        delegate.setMessageListener(listener);
    }

    @Override
    public void run() {
        delegate.run();
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        return delegate.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        return delegate.createTopic(topicName);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        return delegate.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        return delegate.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return delegate.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return delegate.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        delegate.unsubscribe(name);
    }

    /**
     * Wrapper for TopicSubscriber that adds tracing.
     */
    private static class TracingTopicSubscriber extends TracingMessageConsumer implements TopicSubscriber {
        private final TopicSubscriber delegate;

        TracingTopicSubscriber(TopicSubscriber delegate, Destination destination, Tracer tracer,
                JmsContextPropagator contextPropagator) {
            super(delegate, destination, tracer, contextPropagator);
            this.delegate = delegate;
        }

        @Override
        public Topic getTopic() throws JMSException {
            return delegate.getTopic();
        }

        @Override
        public boolean getNoLocal() throws JMSException {
            return delegate.getNoLocal();
        }
    }
}
