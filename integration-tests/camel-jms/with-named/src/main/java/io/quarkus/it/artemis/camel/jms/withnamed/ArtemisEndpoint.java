package io.quarkus.it.artemis.camel.jms.withnamed;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
