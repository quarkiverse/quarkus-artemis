package io.quarkus.it.artemis.camel.jms.withexternal;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("send-and-receive")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisProducerManager namedProducerManager;
    private final ArtemisConsumerManager externalConsumerManager;

    public ArtemisEndpoint(
            ArtemisProducerManager namedProducerManager,
            ArtemisConsumerManager externalConsumerManager) {
        this.namedProducerManager = namedProducerManager;
        this.externalConsumerManager = externalConsumerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        namedProducerManager.send(message);
        return externalConsumerManager.receive();
    }
}
