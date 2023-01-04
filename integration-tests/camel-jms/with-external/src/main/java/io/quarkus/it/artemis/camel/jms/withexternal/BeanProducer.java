package io.quarkus.it.artemis.camel.jms.withexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

public class BeanProducer {
    @Produces
    @Typed({ ConnectionFactory.class })
    @ApplicationScoped
    ActiveMQConnectionFactory externallyDefinedConnectionFactory(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) {
        return new ActiveMQConnectionFactory(externallyDefinedUrl);
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsConsumerManager externalConsumerManager(ConnectionFactory connectionFactory) {
        return new ArtemisJmsConsumerManager(connectionFactory, "out");
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsProducerManager namedProducerManager(ConnectionFactory defaultConnectionFactory) {
        return new ArtemisJmsProducerManager(defaultConnectionFactory, "in");
    }

    BeanProducer() {
    }
}
