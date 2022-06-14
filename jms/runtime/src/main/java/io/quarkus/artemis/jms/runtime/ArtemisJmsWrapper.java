package io.quarkus.artemis.jms.runtime;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public interface ArtemisJmsWrapper {
    ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory cf);
}
