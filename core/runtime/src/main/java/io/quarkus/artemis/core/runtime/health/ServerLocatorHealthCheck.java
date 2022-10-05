package io.quarkus.artemis.core.runtime.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ServerLocatorHealthCheck implements HealthCheck {
    private final Map<String, ServerLocator> serverLocators = new HashMap<>();

    public ServerLocatorHealthCheck(@SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support) {
        Set<String> names = support.getConfiguredNames();
        Set<String> excludedNames = support.getExcludedNames();
        for (String name : names) {
            ServerLocator locator = Arc.container().instance(ServerLocator.class, Identifier.Literal.of(name)).get();
            if (!excludedNames.contains(name) && locator != null) {
                serverLocators.put(name, locator);
            }
        }
    }

    @Override
    public HealthCheckResponse call() {
        if (serverLocators.isEmpty()) {
            return null;
        }
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis Core health check").up();
        for (var entry : serverLocators.entrySet()) {
            String name = entry.getKey();
            ServerLocator serverLocator = entry.getValue();
            try (ClientSessionFactory ignored = serverLocator.createSessionFactory()) {
                builder.withData(name, "UP");
            } catch (Exception e) {
                builder.withData(name, "DOWN").down();
            }
        }
        return builder.build();
    }
}
