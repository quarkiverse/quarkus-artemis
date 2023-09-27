package io.quarkus.it.artemis.jms.withdefault.changeurl;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    public ArtemisEndpoint(
            ArtemisJmsXaProducerManager defaultProducer,
            ArtemisJmsXaConsumerManager defaultConsumer,
            @Identifier("named-1") ArtemisJmsXaProducerManager namedOneProducer,
            @Identifier("named-1") ArtemisJmsXaConsumerManager namedOneConsumer) {
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
}
