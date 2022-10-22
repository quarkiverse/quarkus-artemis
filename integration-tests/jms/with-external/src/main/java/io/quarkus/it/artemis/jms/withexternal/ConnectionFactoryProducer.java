package io.quarkus.it.artemis.jms.withexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.Identifier;

public class ConnectionFactoryProducer {
    @Produces
    @Typed({ ActiveMQConnectionFactory.class, XAConnectionFactory.class, ConnectionFactory.class })
    @ApplicationScoped
    @Identifier("externally-defined")
    ActiveMQConnectionFactory externallyDefinedConnectionFactory(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) {
        return new ActiveMQConnectionFactory(externallyDefinedUrl);
    }
}
