package io.quarkus.it.artemis.camel.jms.withdefaultandnamed;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.common.annotation.Identifier;

@Path("send-and-receive")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisProducerManager defaultProducerManager;
    private final ArtemisConsumerManager namedConsumerManager;

    public ArtemisEndpoint(
            ArtemisProducerManager defaultProducerManager,
            @Identifier("named") ArtemisConsumerManager namedConsumerManager) {
        this.defaultProducerManager = defaultProducerManager;
        this.namedConsumerManager = namedConsumerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        defaultProducerManager.send(message);
        return namedConsumerManager.receive();
    }
}
