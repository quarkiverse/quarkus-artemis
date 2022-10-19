package io.quarkus.artemis.jms.runtime.health;

import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private final HashMap<String, ConnectionFactory> connectionFactories = new HashMap<>();

    public ConnectionFactoryHealthCheck(
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisRuntimeConfigs runtimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support) {
        HashSet<String> includedNames = new HashSet<>(support.getConfiguredNames());
        includedNames.removeAll(support.getExcludedNames());
        for (String name : includedNames) {
            if (runtimeConfigs.getAllConfigs().get(name) != null) {
                ConnectionFactory connectionFactory = Arc.container()
                        .instance(ConnectionFactory.class, Identifier.Literal.of(name)).get();
                if (connectionFactory != null) {
                    connectionFactories.put(name, connectionFactory);
                }
            }
        }
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis JMS health check").up();
        for (var entry : connectionFactories.entrySet()) {
            String name = entry.getKey();
            try (Connection ignored = entry.getValue().createConnection()) {
                builder.withData(name, "UP");
            } catch (Exception e) {
                builder.withData(name, "DOWN").down();
            }
        }
        return builder.build();
    }
}
