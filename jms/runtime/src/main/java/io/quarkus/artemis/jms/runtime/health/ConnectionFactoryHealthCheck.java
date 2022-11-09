package io.quarkus.artemis.jms.runtime.health;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private final HashMap<String, ConnectionFactory> connectionFactories = new HashMap<>();

    public ConnectionFactoryHealthCheck(
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisRuntimeConfigs runtimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisBuildTimeConfigs buildTimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support) {
        HashSet<String> includedNames = new HashSet<>(support.getConfiguredNames());
        includedNames.removeAll(support.getExcludedNames());
        processKnownBeans(runtimeConfigs, includedNames);
        processArcBeans(runtimeConfigs, buildTimeConfigs);
    }

    private void processKnownBeans(ArtemisRuntimeConfigs runtimeConfigs, HashSet<String> includedNames) {
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

    private void processArcBeans(ArtemisRuntimeConfigs runtimeConfigs, ArtemisBuildTimeConfigs buildTimeConfigs) {
        if (runtimeConfigs.getHealthExternalEnabled()) {
            HashSet<String> namesToIgnore = new HashSet<>(runtimeConfigs.getAllConfigs().keySet());
            namesToIgnore.addAll(buildTimeConfigs.getAllConfigs().keySet());
            Map<String, ConnectionFactory> connectionFactoryNamesFromArc = ArtemisUtil
                    .extractIdentifiers(ConnectionFactory.class, namesToIgnore);
            for (var entry : connectionFactoryNamesFromArc.entrySet()) {
                ConnectionFactory connectionFactory = entry.getValue();
                if (connectionFactory != null) {
                    connectionFactories.put(entry.getKey(), connectionFactory);
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
