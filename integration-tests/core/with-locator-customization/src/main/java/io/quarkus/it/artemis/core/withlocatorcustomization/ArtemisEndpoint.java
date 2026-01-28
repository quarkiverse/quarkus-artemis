package io.quarkus.it.artemis.core.withlocatorcustomization;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.activemq.artemis.api.core.client.ServerLocator;

import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ServerLocator defaultLocator;
    private final ServerLocator namedOneLocator;

    public ArtemisEndpoint(
            @SuppressWarnings("CdiInjectionPointsInspection") ServerLocator defaultLocator,
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ServerLocator namedOneLocator) {
        this.defaultLocator = defaultLocator;
        this.namedOneLocator = namedOneLocator;
    }

    @GET
    @Path("/default/consumer-window-size")
    public int getDefaultConsumerWindowSize() {
        return defaultLocator.getConsumerWindowSize();
    }

    @GET
    @Path("/default/call-timeout")
    public long getDefaultCallTimeout() {
        return defaultLocator.getCallTimeout();
    }

    @GET
    @Path("/default/auto-group")
    public boolean getDefaultAutoGroup() {
        return defaultLocator.isAutoGroup();
    }

    @GET
    @Path("/named-1/producer-max-rate")
    public int getNamedOneProducerMaxRate() {
        return namedOneLocator.getProducerMaxRate();
    }

    @GET
    @Path("/named-1/retry-interval-multiplier")
    public double getNamedOneRetryIntervalMultiplier() {
        return namedOneLocator.getRetryIntervalMultiplier();
    }

    @GET
    @Path("/named-1/pre-acknowledge")
    public boolean getNamedOnePreAcknowledge() {
        return namedOneLocator.isPreAcknowledge();
    }
}
