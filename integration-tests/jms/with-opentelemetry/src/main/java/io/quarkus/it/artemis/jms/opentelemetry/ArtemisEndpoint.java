package io.quarkus.it.artemis.jms.opentelemetry;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.it.artemis.jms.common.ArtemisJmsConsumerManager;
import io.quarkus.it.artemis.jms.common.ArtemisJmsProducerManager;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ArtemisJmsProducerManager producer;
    private final ArtemisJmsConsumerManager consumer;

    public ArtemisEndpoint(
            ArtemisJmsProducerManager producer,
            ArtemisJmsConsumerManager consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    @POST
    public void post(String message) {
        producer.send(message);
    }

    @GET
    public String get() {
        return consumer.receive();
    }
}
