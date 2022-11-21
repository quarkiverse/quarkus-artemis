package io.quarkus.artemis.core.runtime.health;

import java.lang.annotation.Annotation;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ServerLocatorHealthCheck implements HealthCheck {
    private final HashMap<String, ServerLocator> serverLocators = new HashMap<>();

    public ServerLocatorHealthCheck(
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
                Annotation identifier;
                if (ArtemisUtil.isDefault(name)) {
                    identifier = Default.Literal.INSTANCE;
                } else {
                    identifier = Identifier.Literal.of(name);
                }
                ServerLocator locator = Arc.container().instance(ServerLocator.class, identifier).get();
                if (locator != null) {
                    serverLocators.put(name, locator);
                }
            }
        }
    }

    private void processArcBeans(ArtemisRuntimeConfigs runtimeConfigs, ArtemisBuildTimeConfigs buildTimeConfigs) {
        if (runtimeConfigs.getHealthExternalEnabled()) {
            HashSet<String> namesToIgnore = new HashSet<>(runtimeConfigs.getAllConfigs().keySet());
            namesToIgnore.addAll(buildTimeConfigs.getAllConfigs().keySet());
            Map<String, ServerLocator> locatorNamesFromArc = ArtemisUtil.extractIdentifiers(ServerLocator.class, namesToIgnore);
            for (var entry : locatorNamesFromArc.entrySet()) {
                ServerLocator locator = entry.getValue();
                if (locator != null) {
                    serverLocators.put(entry.getKey(), locator);
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
