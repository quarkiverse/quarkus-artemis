package io.quarkus.artemis.jms.runtime.health;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.logging.Logger;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private static final Logger LOGGER = Logger.getLogger(ConnectionFactoryHealthCheck.class);

    private final ArtemisRuntimeConfigs runtimeConfigs;
    private final Instance<ConnectionFactory> connectionFactories;
    private final Set<String> connectionFactoryNames;

    public ConnectionFactoryHealthCheck(
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support,
            @Any Instance<ConnectionFactory> connectionFactories) {
        this.runtimeConfigs = runtimeConfigs;
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
                ArtemisUtil.handleFailedHealthCheck(builder, "connection factory", name, LOGGER, runtimeConfigs.healthFailLog(),
                        runtimeConfigs.healthFailLogLevel(), e);
            }
        }
        return builder.build();
    }
}
