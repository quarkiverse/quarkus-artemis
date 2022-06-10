package io.quarkus.it.artemis;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/artemis")
public class ArtemisEndpoint {

    @Inject
    ArtemisProducerManager producer;

    @Inject
    ArtemisConsumerManager consumer;

    @POST
    public void post(String message) {
        producer.send(message);
    }

    @POST
    @Path("/xa")
    @Transactional
    public void postXA(String message) throws Exception {
        producer.sendXA(message);
    }

    @GET
    public String get() {
        return consumer.receive();
    }
}
