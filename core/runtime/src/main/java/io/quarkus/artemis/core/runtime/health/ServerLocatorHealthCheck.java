package io.quarkus.artemis.core.runtime.health;

import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;

import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ServerLocatorHealthCheck implements HealthCheck {
    private final HashMap<String, ServerLocator> serverLocators = new HashMap<>();

    public ServerLocatorHealthCheck(
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisRuntimeConfigs runtimeConfigs,
            @SuppressWarnings("CdiInjectionPointsInspection") ArtemisHealthSupport support) {
        HashSet<String> includedNames = new HashSet<>(support.getConfiguredNames());
        includedNames.removeAll(support.getExcludedNames());
        for (String name : includedNames) {
            if (runtimeConfigs.getAllConfigs().get(name) != null) {
                ServerLocator locator = Arc.container().instance(ServerLocator.class, Identifier.Literal.of(name)).get();
                if (locator != null) {
                    serverLocators.put(name, locator);
                }
            }
        }
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis Core health check").up();
        for (var entry : serverLocators.entrySet()) {
            String name = entry.getKey();
            try (ClientSessionFactory ignored = entry.getValue().createSessionFactory()) {
                builder.withData(name, "UP");
            } catch (Exception e) {
                builder.withData(name, "DOWN").down();
            }
        }
        return builder.build();
    }
}
