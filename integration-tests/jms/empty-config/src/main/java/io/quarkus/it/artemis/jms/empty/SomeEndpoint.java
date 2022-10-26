package io.quarkus.it.artemis.jms.empty;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/artemis")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class SomeEndpoint {
    @GET
    public String hello() {
        return "Hello";
    }
}