package io.quarkus.it.artemis.jms.withexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.it.artemis.jms.common.ArtemisJmsXaConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @Typed({ ActiveMQConnectionFactory.class, XAConnectionFactory.class, ConnectionFactory.class })
    @ApplicationScoped
    @Identifier("externally-defined")
    ActiveMQConnectionFactory externallyDefinedConnectionFactory(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) {
        return new ActiveMQConnectionFactory(externallyDefinedUrl);
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisJmsXaConsumerManager externallyDefinedManager(
            @Identifier("externally-defined") ConnectionFactory externallyDefinedConnectionFactory,
            @Identifier("externally-defined") XAConnectionFactory externallyDefinedXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaConsumerManager(
                externallyDefinedConnectionFactory,
                externallyDefinedXaConnectionFactory,
                tm,
                "test-jms-externally-defined");
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisJmsXaProducerManager externallyDefinedProducer(
            @Identifier("externally-defined") ConnectionFactory namedOneConnectionFactory,
            @Identifier("externally-defined") XAConnectionFactory namedOneXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaProducerManager(
                namedOneConnectionFactory,
                namedOneXaConnectionFactory,
                tm,
                "test-jms-externally-defined");
    }

    BeanProducer() {
    }
}
