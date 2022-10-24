package io.quarkus.it.artemis.core.withdefaultandexternal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.Identifier;

public class ServerLocatorProducer {
    @Produces
    @ApplicationScoped
    @Identifier("externally-defined")
    ServerLocator externallyDefinedServerLocator(
            @ConfigProperty(name = "artemis.externally-defined.url") String externallyDefinedUrl) throws Exception {
        return ActiveMQClient.createServerLocator(externallyDefinedUrl);
    }
}
