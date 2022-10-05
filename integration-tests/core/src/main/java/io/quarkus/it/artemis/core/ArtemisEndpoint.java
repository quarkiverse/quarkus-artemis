package io.quarkus.it.artemis.core;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {

    @Inject
    ArtemisProducerManager defaultProducer;

    @Inject
    ArtemisConsumerManager defaultConsumer;

    @Inject
    @Identifier("named-1")
    ArtemisProducerManager namedOneProducer;

    @Inject
    @Identifier("named-1")
    ArtemisConsumerManager namedOneConsumer;

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
    public String namedOneGet(String message) {
        return namedOneConsumer.receive();
    }
}
