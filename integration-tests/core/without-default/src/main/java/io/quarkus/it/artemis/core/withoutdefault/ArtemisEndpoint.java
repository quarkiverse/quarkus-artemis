package io.quarkus.it.artemis.core.withoutdefault;

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
    private final ArtemisCoreProducerManager namedOneProducer;
    private final ArtemisCoreConsumerManager namedOneConsumer;

    public ArtemisEndpoint(
            @Identifier("named-1") ArtemisCoreProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisCoreConsumerManager namedOneConsumer) {
        this.namedOneProducer = namedOneProducer;
        this.namedOneConsumer = namedOneConsumer;
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
}
