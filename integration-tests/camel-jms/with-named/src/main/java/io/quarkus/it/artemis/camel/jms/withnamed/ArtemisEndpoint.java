package io.quarkus.it.artemis.camel.jms.withnamed;

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
    private final ArtemisJmsProducerManager defaultProducerManager;
    private final ArtemisJmsConsumerManager namedConsumerManager;

    public ArtemisEndpoint(ArtemisJmsProducerManager defaultProducerManager, ArtemisJmsConsumerManager namedConsumerManager) {
        this.defaultProducerManager = defaultProducerManager;
        this.namedConsumerManager = namedConsumerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        defaultProducerManager.send(message);
        return namedConsumerManager.receive();
    }
}
