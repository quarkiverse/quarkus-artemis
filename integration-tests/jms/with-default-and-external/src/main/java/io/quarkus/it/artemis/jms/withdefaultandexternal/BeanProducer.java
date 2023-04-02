package io.quarkus.it.artemis.jms.withdefaultandexternal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.XAConnectionFactory;
import jakarta.transaction.TransactionManager;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @Typed({ XAConnectionFactory.class, ConnectionFactory.class })
    @ApplicationScoped
    @Identifier("externally-defined")
    ActiveMQConnectionFactory externallyDefinedConnectionFactory(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) {
        return new ActiveMQConnectionFactory(externallyDefinedUrl);
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsConsumerManager defaultConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory) {
        return new ArtemisJmsConsumerManager(connectionFactory, "test-jms-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisJmsConsumerManager namedOneConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory) {
        return new ArtemisJmsConsumerManager(namedOneConnectionFactory, "test-jms-named-1");
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisJmsConsumerManager externallyDefinedConsumer(
            @Identifier("externally-defined") ConnectionFactory namedOneConnectionFactory) {
        return new ArtemisJmsConsumerManager(namedOneConnectionFactory, "test-jms-externally-defined");
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsXaProducerManager defaultProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") XAConnectionFactory defaultXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaProducerManager(defaultConnectionFactory, defaultXaConnectionFactory, tm,
                "test-jms-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisJmsXaProducerManager namedOneProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") XAConnectionFactory namedOneXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaProducerManager(
                namedOneConnectionFactory,
                namedOneXaConnectionFactory,
                tm,
                "test-jms-named-1");
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisJmsXaProducerManager externallyDefinedProducer(
            @Identifier("externally-defined") ConnectionFactory externallyDefinedConnectionFactory,
            @Identifier("externally-defined") XAConnectionFactory externallyDefinedXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaProducerManager(
                externallyDefinedConnectionFactory,
                externallyDefinedXaConnectionFactory,
                tm,
                "test-jms-externally-defined");
    }

    BeanProducer() {
    }
}
