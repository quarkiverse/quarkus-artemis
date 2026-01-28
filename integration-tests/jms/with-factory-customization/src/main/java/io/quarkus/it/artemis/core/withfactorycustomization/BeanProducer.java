package io.quarkus.it.artemis.core.withfactorycustomization;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    ArtemisJmsConsumerManager defaultConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory) {
        return new ArtemisJmsConsumerManager(defaultConnectionFactory, "test-jms-default");
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
    ArtemisJmsProducerManager defaultProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory) {
        return new ArtemisJmsProducerManager(defaultConnectionFactory, "test-jms-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisJmsProducerManager namedOneProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory) {
        return new ArtemisJmsProducerManager(namedOneConnectionFactory, "test-jms-named-1");
    }

    BeanProducer() {
    }
}
