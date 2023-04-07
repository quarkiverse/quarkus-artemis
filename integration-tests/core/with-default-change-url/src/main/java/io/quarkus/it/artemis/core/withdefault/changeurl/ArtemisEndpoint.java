package io.quarkus.it.artemis.core.withdefault.changeurl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

    public ArtemisEndpoint(
            ArtemisCoreProducerManager defaultProducer,
            ArtemisCoreConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisCoreProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisCoreConsumerManager namedOneConsumer) {
        this.defaultProducer = defaultProducer;
        this.defaultConsumer = defaultConsumer;
        this.namedOneProducer = namedOneProducer;
        this.namedOneConsumer = namedOneConsumer;
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
}
