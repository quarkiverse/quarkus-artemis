package io.quarkus.it.artemis.jms.withexternal;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsXaProducerManager;
import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsXaProducerManager externallyDefinedProducer;
    private final ArtemisJmsConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            @Identifier("externally-defined") ArtemisJmsXaProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisJmsConsumerManager externallyDefinedConsumer) {
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
}
