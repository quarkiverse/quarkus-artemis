package io.quarkus.it.artemis.jms.opentelemetry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    ArtemisJmsConsumerManager consumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory) {
        return new ArtemisJmsConsumerManager(connectionFactory, "test-jms-otel");
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsProducerManager producerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory) {
        return new ArtemisJmsProducerManager(connectionFactory, "test-jms-otel");
    }

    BeanProducer() {
    }
}
