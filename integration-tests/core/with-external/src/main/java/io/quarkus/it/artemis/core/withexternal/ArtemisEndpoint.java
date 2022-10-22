package io.quarkus.it.artemis.core.withexternal;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisProducerManager externallyDefinedProducer;
    private final ArtemisConsumerManager externallyDefinedConsumer;

    public ArtemisEndpoint(
            @Identifier("externally-defined") ArtemisProducerManager externallyDefinedProducer,
            @Identifier("externally-defined") ArtemisConsumerManager externallyDefinedConsumer) {
        this.externallyDefinedProducer = externallyDefinedProducer;
        this.externallyDefinedConsumer = externallyDefinedConsumer;
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
