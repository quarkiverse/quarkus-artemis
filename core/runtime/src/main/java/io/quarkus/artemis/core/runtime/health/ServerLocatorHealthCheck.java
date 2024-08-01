package io.quarkus.artemis.core.runtime.health;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;

@Readiness
@ApplicationScoped
public class ServerLocatorHealthCheck implements HealthCheck {
    private final Instance<ServerLocator> serverLocators;
    private final Set<String> serverLocatorNames;

    public ServerLocatorHealthCheck(
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support,
            @Any Instance<ServerLocator> serverLocators) {
        this.serverLocators = serverLocators;
        serverLocatorNames = support.getConfiguredNames().stream()
                .filter(name -> runtimeConfigs.configs().get(name).isHealthInclude())
                .collect(Collectors.toCollection(HashSet::new));
        serverLocatorNames.addAll(ArtemisUtil.getExternalNames(ServerLocator.class, runtimeConfigs, buildTimeConfigs));
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis Core health check").up();
        for (String name : serverLocatorNames) {
            Annotation identifier = ArtemisUtil.toIdentifier(name);
            try (ClientSessionFactory ignored = serverLocators.select(identifier).get().createSessionFactory()) {
                builder.withData(name, "UP");
            } catch (Exception e) {
                builder.withData(name, "DOWN").down();
            }
        }
        return builder.build();
    }
}
