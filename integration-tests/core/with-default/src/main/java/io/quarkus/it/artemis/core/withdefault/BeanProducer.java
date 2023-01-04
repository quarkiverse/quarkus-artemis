package io.quarkus.it.artemis.core.withdefault;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.activemq.artemis.api.core.client.*;

import io.quarkus.it.artemis.core.common.ArtemisCoreConsumerManager;
import io.quarkus.it.artemis.core.common.ArtemisCoreProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    ArtemisCoreConsumerManager defaultConsumer(@SuppressWarnings("CdiInjectionPointsInspection") ServerLocator serverLocator)
            throws Exception {
        return new ArtemisCoreConsumerManager(serverLocator, "test-core-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisCoreConsumerManager namedOneConsumer(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneServerLocator)
            throws Exception {
        return new ArtemisCoreConsumerManager(namedOneServerLocator, "test-core-named-1");
    }

    @Produces
    @ApplicationScoped
    public static ArtemisCoreProducerManager defaultProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") ServerLocator serverLocator) throws Exception {
        return new ArtemisCoreProducerManager(serverLocator, "test-core-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    public static ArtemisCoreProducerManager namedOneProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneServerLocator)
            throws Exception {
        return new ArtemisCoreProducerManager(namedOneServerLocator, "test-core-named-1");
    }

    BeanProducer() {
    }
}
