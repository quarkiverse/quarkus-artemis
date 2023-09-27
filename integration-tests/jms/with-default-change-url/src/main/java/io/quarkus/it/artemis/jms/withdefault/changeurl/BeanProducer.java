package io.quarkus.it.artemis.jms.withdefault.changeurl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.XAConnectionFactory;
import jakarta.transaction.TransactionManager;

import io.quarkus.it.artemis.jms.common.ArtemisJmsXaConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    ArtemisJmsXaConsumerManager defaultConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") XAConnectionFactory defaultXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaConsumerManager(defaultConnectionFactory, defaultXaConnectionFactory, tm, "test-jms-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisJmsXaConsumerManager namedOneConsumerManager(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") XAConnectionFactory namedOneXaConnectionFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager tm) {
        return new ArtemisJmsXaConsumerManager(namedOneConnectionFactory, namedOneXaConnectionFactory, tm, "test-jms-named-1");
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

    BeanProducer() {
    }

}
