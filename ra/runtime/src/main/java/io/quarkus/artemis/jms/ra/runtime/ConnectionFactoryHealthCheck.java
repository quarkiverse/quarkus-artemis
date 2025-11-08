package io.quarkus.artemis.jms.ra.runtime;

import java.lang.annotation.Annotation;
import java.util.Set;

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

import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;

@Readiness
@ApplicationScoped
public class ConnectionFactoryHealthCheck implements HealthCheck {
    private static final Logger LOGGER = Logger.getLogger(ConnectionFactoryHealthCheck.class);

    private final Instance<ConnectionFactory> connectionFactories;
    private final Set<String> connectionFactoryNames;
    private final ArtemisRuntimeConfigs runtimeConfigs;

    public ConnectionFactoryHealthCheck(
            IronJacamarBuildtimeConfig buildTimeConfigs,
            @Any Instance<ConnectionFactory> connectionFactories,
            ArtemisRuntimeConfigs runtimeConfigs) {
        this.connectionFactories = connectionFactories;
        connectionFactoryNames = buildTimeConfigs.resourceAdapters().keySet();
        this.runtimeConfigs = runtimeConfigs;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Artemis JMS Resource Adaptor health check").up();
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
