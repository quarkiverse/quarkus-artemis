package io.quarkus.it.artemis.core.withdefaultandexternal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.it.artemis.core.common.ArtemisCoreConsumerManager;
import io.quarkus.it.artemis.core.common.ArtemisCoreProducerManager;
import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisCoreProducerManager defaultProducer;
    private final ArtemisCoreConsumerManager defaultConsumer;
    private final ArtemisCoreProducerManager namedOneProducer;
    private final ArtemisCoreConsumerManager namedOneConsumer;
    private final ArtemisCoreProducerManager externallyDefinedProducer;
    private final ArtemisCoreConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            ArtemisCoreProducerManager defaultProducer,
            ArtemisCoreConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisCoreProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisCoreConsumerManager namedOneConsumer,
            @Identifier("externally-defined") ArtemisCoreProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisCoreConsumerManager externallyDefinedConsumer) {
        this.defaultProducer = defaultProducer;
        this.defaultConsumer = defaultConsumer;
        this.namedOneProducer = namedOneProducer;
        this.namedOneConsumer = namedOneConsumer;
        this.externallyDefinedProducer = externallyDefinedProducer;
        this.externallyDefinedConsumer = externallyDefinedConsumer;
    }

    @POST
    public void defaultPost(String message) {
        defaultProducer.send(message);
    }

    @GET
    public String defaultGet() {
        return defaultConsumer.receive();
    }

    @POST
    @Path("named-1")
    public void namedOnePost(String message) {
        namedOneProducer.send(message);
    }

    @GET
    @Path("named-1")
    public String namedOneGet() {
        return namedOneConsumer.receive();
    }

    @POST
    @Path("externally-defined")
    public void externallyDefinedPost(String message) {
        externallyDefinedProducer.send(message);
    }

    @GET
    @Path("externally-defined")
    public String externallyDefinedGet() {
        return externallyDefinedConsumer.receive();
    }
}
