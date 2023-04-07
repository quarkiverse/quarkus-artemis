package io.quarkus.it.artemis.jms.withdefault;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    public ArtemisEndpoint(
            ArtemisJmsXaProducerManager defaultProducer,
            ArtemisJmsConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisJmsXaProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisJmsConsumerManager namedOneConsumer) {
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
}
