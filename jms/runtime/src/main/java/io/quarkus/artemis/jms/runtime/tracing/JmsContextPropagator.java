package io.quarkus.artemis.jms.runtime.tracing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

/**
 * Helper class for propagating OpenTelemetry context through JMS messages.
 * Uses W3C Trace Context standard for propagation.
 */
class JmsContextPropagator {

    private static final TextMapGetter<Message> GETTER = new TextMapGetter<Message>() {
        @Override
        public Iterable<String> keys(Message carrier) {
            Map<String, String> properties = new HashMap<>();
            try {
                Enumeration<?> propertyNames = carrier.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = (String) propertyNames.nextElement();
                    properties.put(key, carrier.getStringProperty(key));
                }
            } catch (JMSException e) {
                // Ignore - return empty keys
            }
            return properties.keySet();
        }

        @Override
        public String get(Message carrier, String key) {
            if (carrier == null || key == null) {
                return null;
            }
            try {
                return carrier.getStringProperty(key);
            } catch (JMSException e) {
                return null;
            }
        }
    };

    private static final TextMapSetter<Message> MESSAGE_SETTER = new TextMapSetter<Message>() {
        @Override
        public void set(Message carrier, String key, String value) {
            if (carrier == null || key == null || value == null) {
                return;
            }
            try {
                carrier.setStringProperty(key, value);
            } catch (JMSException e) {
                // Ignore - best effort
            }
        }
    };

    /**
     * Setter for JMSProducer properties. Properties set on a JMSProducer are applied
     * to all messages it subsequently sends, enabling context propagation for
     * convenience send methods (String, Map, byte[], Serializable) where the Message
     * object is created internally by the JMS provider.
     */
    private static final TextMapSetter<JMSProducer> PRODUCER_SETTER = new TextMapSetter<JMSProducer>() {
        @Override
        public void set(JMSProducer carrier, String key, String value) {
            if (carrier == null || key == null || value == null) {
                return;
            }
            carrier.setProperty(key, value);
        }
    };

    private final ContextPropagators propagators;

    JmsContextPropagator(ContextPropagators propagators) {
        this.propagators = propagators;
    }

    /**
     * Extracts OpenTelemetry context from JMS message properties.
     *
     * @param message the JMS message
     * @return the extracted context
     */
    Context extractContext(Message message) {
        return propagators.getTextMapPropagator().extract(Context.current(), message, GETTER);
    }

    /**
     * Injects OpenTelemetry context into JMS message properties.
     *
     * @param context the context to inject
     * @param message the JMS message
     */
    void injectContext(Context context, Message message) {
        propagators.getTextMapPropagator().inject(context, message, MESSAGE_SETTER);
    }

    /**
     * Injects OpenTelemetry context into JMSProducer properties.
     * Properties set on the producer are applied to all messages it sends,
     * enabling trace context propagation for convenience send methods.
     *
     * @param context the context to inject
     * @param producer the JMS producer
     */
    void injectContext(Context context, JMSProducer producer) {
        propagators.getTextMapPropagator().inject(context, producer, PRODUCER_SETTER);
    }
}
