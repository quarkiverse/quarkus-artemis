package io.quarkus.it.artemis.core.empty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/artemis")
@Produces(MediaType.TEXT_PLAIN)
public class SomeEndpoint {
    @GET
    public String hello() {
        return "Hello";
    }
}
