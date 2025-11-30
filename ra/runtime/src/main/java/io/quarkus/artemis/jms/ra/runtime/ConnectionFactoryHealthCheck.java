package io.quarkus.artemis.jms.ra.runtime;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisUtil;

@Readiness
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private final Instance<ConnectionFactory> connectionFactories;
    private final Set<String> connectionFactoryNames;

    public ConnectionFactoryHealthCheck(IronJacamarBuildtimeConfig buildTimeConfigs,
            @Any Instance<ConnectionFactory> connectionFactories) {
        this.connectionFactories = connectionFactories;
        connectionFactoryNames = buildTimeConfigs.resourceAdapters().keySet();
    }

    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis JMS Resource Adaptor health check").up();
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
