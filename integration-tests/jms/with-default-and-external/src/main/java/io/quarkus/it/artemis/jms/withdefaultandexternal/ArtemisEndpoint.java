package io.quarkus.it.artemis.jms.withdefaultandexternal;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsXaProducerManager defaultProducer;
    private final ArtemisJmsConsumerManager defaultConsumer;
    private final ArtemisJmsXaProducerManager namedOneProducer;
    private final ArtemisJmsConsumerManager namedOneConsumer;
    private final ArtemisJmsXaProducerManager externallyDefinedProducer;
    private final ArtemisJmsConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            ArtemisJmsXaProducerManager defaultProducer,
            ArtemisJmsConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisJmsXaProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisJmsConsumerManager namedOneConsumer,
            @Identifier("externally-defined") ArtemisJmsXaProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisJmsConsumerManager externallyDefinedConsumer) {
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
    @Path("/xa")
    @Transactional
    public void defaultPostXA(String message) throws Exception {
        defaultProducer.sendXA(message);
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
    @Path("named-1/xa")
    @Transactional
    public void namedOnePostXA(String message) throws Exception {
        namedOneProducer.sendXA(message);
    }

    @POST
    @Path("externally-defined")
    public void externallyDefinedPost(String message) {
        externallyDefinedProducer.send(message);
    }

    @POST
    @Path("externally-defined/xa")
    @Transactional
    public void externallyDefinedPostXA(String message) throws Exception {
        externallyDefinedProducer.sendXA(message);
    }

    @GET
    @Path("externally-defined")
    public String externallyDefinedGet() {
        return externallyDefinedConsumer.receive();
    }
}
