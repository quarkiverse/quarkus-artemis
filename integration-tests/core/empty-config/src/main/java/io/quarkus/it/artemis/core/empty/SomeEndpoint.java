package io.quarkus.it.artemis.core.empty;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/artemis")
@Produces(MediaType.TEXT_PLAIN)
public class SomeEndpoint {
    @GET
    public String hello() {
        return "Hello";
    }
}
