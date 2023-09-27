package io.quarkus.it.artemis.jms.withdefaultandexternal;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsXaConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsXaProducerManager defaultProducer;
    private final ArtemisJmsXaConsumerManager defaultConsumer;
    private final ArtemisJmsXaProducerManager namedOneProducer;
    private final ArtemisJmsXaConsumerManager namedOneConsumer;
    private final ArtemisJmsXaProducerManager externallyDefinedProducer;
    private final ArtemisJmsXaConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            ArtemisJmsXaProducerManager defaultProducer,
            ArtemisJmsXaConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisJmsXaProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisJmsXaConsumerManager namedOneConsumer,
            @Identifier("externally-defined") ArtemisJmsXaProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisJmsXaConsumerManager externallyDefinedConsumer) {
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

    @GET
    @Path("/xa")
    @Transactional
    public String defaultGetXACommit() throws Exception {
        return defaultConsumer.receiveXA(false);
    }

    @GET
    @Path("/xa-rollback")
    @Transactional
    public String defaultGetXARollback() throws Exception {
        return defaultConsumer.receiveXA(true);
    }

    @GET
    @Path("named-1/xa")
    @Transactional
    public String namedOneGetXACommit() throws Exception {
        return namedOneConsumer.receiveXA(false);
    }

    @GET
    @Path("named-1/xa-rollback")
    @Transactional
    public String namedOneGetXARollback() throws Exception {
        return namedOneConsumer.receiveXA(true);
    }

    @GET
    @Path("externally-defined/xa")
    @Transactional
    public String externallyDefinedXACommit() throws Exception {
        return externallyDefinedConsumer.receiveXA(false);
    }

    @GET
    @Path("externally-defined/xa-rollback")
    @Transactional
    public String externallyDefinedXARollback() throws Exception {
        return externallyDefinedConsumer.receiveXA(true);
    }
}
