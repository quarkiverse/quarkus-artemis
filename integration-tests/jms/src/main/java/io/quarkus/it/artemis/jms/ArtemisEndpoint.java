package io.quarkus.it.artemis.jms;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

    @POST
    @Path("/xa")
    @Transactional
    public void defaultPostXA(String message) throws Exception {
        defaultProducer.sendXA(message);
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

    @POST
    @Path("named-1/xa")
    @Transactional
    public void namedOnePostXA(String message) throws Exception {
        namedOneProducer.sendXA(message);
    }

    @GET
    @Path("named-1")
    public String namedOneGet() {
        return namedOneConsumer.receive();
    }
}
