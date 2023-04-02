package io.quarkus.it.artemis.core.withdefaultandexternal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.it.artemis.core.common.ArtemisCoreConsumerManager;
import io.quarkus.it.artemis.core.common.ArtemisCoreProducerManager;
import io.smallrye.common.annotation.Identifier;

public class BeanProducer {
    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ServerLocator externallyDefinedServerLocator(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) throws Exception {
        return ActiveMQClient.createServerLocator(externallyDefinedUrl);
    }

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
    @Identifier("externally-defined")
    ArtemisCoreConsumerManager externallyDefinedConsumer(
            @Identifier("externally-defined") ServerLocator externallyAddedServerLocator)
            throws Exception {
        return new ArtemisCoreConsumerManager(externallyAddedServerLocator, "test-core-externally-defined");
    }

    @Produces
    @ApplicationScoped
    ArtemisCoreProducerManager defaultProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") ServerLocator serverLocator) throws Exception {
        return new ArtemisCoreProducerManager(serverLocator, "test-core-default");
    }

    @Produces
    @ApplicationScoped
    @Identifier("named-1")
    ArtemisCoreProducerManager namedOneProducer(
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneServerLocator)
            throws Exception {
        return new ArtemisCoreProducerManager(namedOneServerLocator, "test-core-named-1");
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisCoreProducerManager externallyAddedProducer(
            @Identifier("externally-defined") ServerLocator externallyAddedServerLocator) throws Exception {
        return new ArtemisCoreProducerManager(externallyAddedServerLocator, "test-core-externally-defined");
    }

    BeanProducer() {
    }
}
