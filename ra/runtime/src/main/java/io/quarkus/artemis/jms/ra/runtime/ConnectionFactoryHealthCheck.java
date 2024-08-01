package io.quarkus.artemis.jms.ra.runtime;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.smallrye.common.annotation.Identifier;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private final HashMap<String, ConnectionFactory> connectionFactories = new HashMap<>();

    public ConnectionFactoryHealthCheck(IronJacamarBuildtimeConfig buildTimeConfigs) {
        processKnownBeans(buildTimeConfigs);
    }

    private void processKnownBeans(IronJacamarBuildtimeConfig buildTimeConfigs) {
        for (String name : buildTimeConfigs.resourceAdapters().keySet()) {
            buildTimeConfigs.resourceAdapters().get(name).ra().kind().ifPresent(kind -> {
                if (kind.equals("artemis")) {
                    Annotation identifier;
                    if (ArtemisUtil.isDefault(name)) {
                        identifier = Default.Literal.INSTANCE;
                    } else {
                        identifier = Identifier.Literal.of(name);
                    }
                    ConnectionFactory connectionFactory;
                    try (InstanceHandle<ConnectionFactory> handle = Arc.container().instance(ConnectionFactory.class,
                            identifier)) {
                        connectionFactory = handle.get();
                    }
                    if (connectionFactory != null) {
                        connectionFactories.put(name, connectionFactory);
                    }
                }
            });
        }
    }

    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis JMS Resource Adaptor health check").up();
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
