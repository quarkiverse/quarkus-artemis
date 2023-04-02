package io.quarkus.it.artemis.camel.jms.withnamedandexternal;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;
import io.smallrye.common.annotation.Identifier;

@Path("send-and-receive")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsProducerManager namedProducerManager;
    private final ArtemisJmsConsumerManager externalConsumerManager;

    public ArtemisEndpoint(
            @Identifier("named") ArtemisJmsProducerManager namedProducerManager,
            @Identifier("externally-defined") ArtemisJmsConsumerManager externalConsumerManager) {
        this.namedProducerManager = namedProducerManager;
        this.externalConsumerManager = externalConsumerManager;
    }

    @POST
    public String sendAndReceive(String message) {
        namedProducerManager.send(message);
        return externalConsumerManager.receive();
    }
}
