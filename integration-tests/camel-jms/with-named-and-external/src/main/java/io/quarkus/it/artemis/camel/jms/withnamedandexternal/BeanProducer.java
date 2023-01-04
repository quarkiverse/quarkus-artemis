package io.quarkus.it.artemis.camel.jms.withnamedandexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @Typed({ ConnectionFactory.class })
    @ApplicationScoped
    @Identifier("externally-defined")
    ActiveMQConnectionFactory externallyDefinedConnectionFactory(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) {
        return new ActiveMQConnectionFactory(externallyDefinedUrl);
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisJmsConsumerManager externalConsumerManager(
            @Identifier("externally-defined") ConnectionFactory connectionFactory) {
        return new ArtemisJmsConsumerManager(connectionFactory, "out");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named")
    ArtemisJmsProducerManager namedProducerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named") ConnectionFactory defaultConnectionFactory) {
        return new ArtemisJmsProducerManager(defaultConnectionFactory, "in");
    }

    BeanProducer() {
    }
}
