package io.quarkus.it.artemis.core.withfactorycustomization;

import jakarta.jms.ConnectionFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.arc.ClientProxy;
import io.smallrye.common.annotation.Identifier;

@Path("/artemis")
@Produces(MediaType.TEXT_PLAIN)
public class ArtemisEndpoint {
    private final ActiveMQConnectionFactory defaultFactory;
    private final ActiveMQConnectionFactory namedOneFactory;

    public ArtemisEndpoint(
            @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory defaultFactory,
            @SuppressWarnings("CdiInjectionPointsInspection") @Identifier("named-1") ConnectionFactory namedOneFactory) {
        this.defaultFactory = (ActiveMQConnectionFactory) ClientProxy.unwrap(defaultFactory);
        this.namedOneFactory = (ActiveMQConnectionFactory) ClientProxy.unwrap(namedOneFactory);
    }

    @GET
    @Path("/default/consumer-window-size")
    public int getDefaultConsumerWindowSize() {
        return defaultFactory.getConsumerWindowSize();
    }

    @GET
    @Path("/default/call-timeout")
    public long getDefaultCallTimeout() {
        return defaultFactory.getCallTimeout();
    }

    @GET
    @Path("/default/auto-group")
    public boolean getDefaultAutoGroup() {
        return defaultFactory.isAutoGroup();
    }

    @GET
    @Path("/named-1/producer-max-rate")
    public int getNamedOneProducerMaxRate() {
        return namedOneFactory.getProducerMaxRate();
    }

    @GET
    @Path("/named-1/retry-interval-multiplier")
    public double getNamedOneRetryIntervalMultiplier() {
        return namedOneFactory.getRetryIntervalMultiplier();
    }

    @GET
    @Path("/named-1/pre-acknowledge")
    public boolean getNamedOnePreAcknowledge() {
        return namedOneFactory.isPreAcknowledge();
    }
}
