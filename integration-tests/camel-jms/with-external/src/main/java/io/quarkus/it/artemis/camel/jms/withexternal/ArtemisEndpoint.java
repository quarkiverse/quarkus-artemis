package io.quarkus.it.artemis.camel.jms.withexternal;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

@Path("send-and-receive")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsProducerManager namedProducerManager;
    private final ArtemisJmsConsumerManager externalConsumerManager;

    public ArtemisEndpoint(ArtemisJmsProducerManager namedProducerManager, ArtemisJmsConsumerManager externalConsumerManager) {
        this.namedProducerManager = namedProducerManager;
        this.externalConsumerManager = externalConsumerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        namedProducerManager.send(message);
        return externalConsumerManager.receive();
    }
}
