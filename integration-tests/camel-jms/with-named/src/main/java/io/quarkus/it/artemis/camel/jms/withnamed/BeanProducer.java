package io.quarkus.it.artemis.camel.jms.withnamed;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    ArtemisJmsConsumerManager namedConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory) {
        return new ArtemisJmsConsumerManager(connectionFactory, "out");
    }

    @Produces
    @ApplicationScoped
    ArtemisJmsProducerManager defaultProducerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory) {
        return new ArtemisJmsProducerManager(defaultConnectionFactory, "in");
    }

    BeanProducer() {
    }
}
