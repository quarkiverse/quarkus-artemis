package io.quarkus.artemis.jms.runtime;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import jakarta.jms.ConnectionFactory;

public interface ArtemisJmsWrapper {
    ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory cf);
}
