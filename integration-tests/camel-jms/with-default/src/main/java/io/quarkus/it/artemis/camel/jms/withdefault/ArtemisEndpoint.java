package io.quarkus.it.artemis.camel.jms.withdefault;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("send-and-receive")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisConsumerManager consumerManager;
    private final ArtemisProducerManager producerManager;

    public ArtemisEndpoint(
            ArtemisConsumerManager consumerManager,
            ArtemisProducerManager producerManager) {
        this.consumerManager = consumerManager;
        this.producerManager = producerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        producerManager.send(message);
        return consumerManager.receive();
    }
}
