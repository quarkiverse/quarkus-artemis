package io.quarkus.it.artemis.core.withexternal;

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
    @Identifier("externally-defined")
    ArtemisCoreConsumerManager externallyDefinedConsumer(
            @Identifier("externally-defined") ServerLocator externallyAddedServerLocator) throws Exception {
        return new ArtemisCoreConsumerManager(externallyAddedServerLocator, "test-core-externally-defined");
    }

    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ArtemisCoreProducerManager externallyAddedProducer(
            @Identifier("externally-defined") ServerLocator externallyAddedServerLocator)
            throws Exception {
        return new ArtemisCoreProducerManager(externallyAddedServerLocator, "test-core-externally-defined");
    }

    BeanProducer() {
    }
}
