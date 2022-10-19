package io.quarkus.it.artemis.core.empty;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/artemis")
@Produces(MediaType.TEXT_PLAIN)
public class SomeEndpoint {
    @GET
    public String hello() {
        return "Hello";
    }
}
