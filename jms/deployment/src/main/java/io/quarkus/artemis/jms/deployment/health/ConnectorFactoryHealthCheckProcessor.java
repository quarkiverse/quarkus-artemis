package io.quarkus.artemis.jms.deployment.health;

import java.util.Optional;

import io.quarkus.artemis.core.deployment.health.ArtemisHealthSupportBuildItem;
import io.quarkus.artemis.jms.runtime.health.ConnectionFactoryHealthCheck;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ConnectorFactoryHealthCheckProcessor {
    @SuppressWarnings("unused")
    @BuildStep()
    HealthBuildItem healthChecks(
            Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (artemisHealthSupportBuildItem.isEmpty()) {
            return null;
        }
        return new HealthBuildItem(ConnectionFactoryHealthCheck.class.getCanonicalName(), true);
    }
}
