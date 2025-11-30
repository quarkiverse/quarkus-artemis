package io.quarkus.artemis.jms.runtime.health;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;

@Readiness
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private final Instance<ConnectionFactory> connectionFactories;
    private final Set<String> connectionFactoryNames;

    public ConnectionFactoryHealthCheck(
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support,
            @Any Instance<ConnectionFactory> connectionFactories) {
        this.connectionFactories = connectionFactories;
        connectionFactoryNames = support.getConfiguredNames().stream()
                .filter(name -> runtimeConfigs.configs().get(name).isHealthInclude())
                .collect(Collectors.toCollection(HashSet::new));
        connectionFactoryNames.addAll(ArtemisUtil.getExternalNames(ConnectionFactory.class, runtimeConfigs, buildTimeConfigs));
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis JMS health check").up();
        for (String name : connectionFactoryNames) {
            Annotation identifier = ArtemisUtil.toIdentifier(name);
            try (Connection ignored = connectionFactories.select(identifier).get().createConnection()) {
                builder.withData(name, "UP");
            } catch (Exception e) {
                builder.withData(name, "DOWN").down();
            }
        }
        return builder.build();
    }
}
