package io.quarkus.artemis.jms.runtime.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {

    private final Map<String, ConnectionFactory> connectionFactories = new HashMap<>();

    public ConnectionFactoryHealthCheck(
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisRuntimeConfigs runtimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support) {
        Set<String> names = support.getConfiguredNames();
        Set<String> excludedNames = support.getExcludedNames();
        for (String name : names) {
            if (runtimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig()).getUrl() != null) {
                ConnectionFactory connectionFactory = Arc.container()
                        .instance(ConnectionFactory.class, Identifier.Literal.of(name)).get();
                if (!excludedNames.contains(name) && connectionFactory != null) {
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
