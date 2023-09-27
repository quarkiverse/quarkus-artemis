package io.quarkus.it.artemis.jms.withexternal;

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
    private final ArtemisJmsXaProducerManager externallyDefinedProducer;
    private final ArtemisJmsXaConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            @Identifier("externally-defined") ArtemisJmsXaProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisJmsXaConsumerManager externallyDefinedConsumer) {
        this.externallyDefinedProducer = externallyDefinedProducer;
        this.externallyDefinedConsumer = externallyDefinedConsumer;
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
