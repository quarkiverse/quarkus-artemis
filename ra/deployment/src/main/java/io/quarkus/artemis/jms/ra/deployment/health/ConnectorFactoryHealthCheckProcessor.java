package io.quarkus.artemis.jms.ra.deployment.health;

import java.util.Optional;

import io.quarkus.artemis.core.deployment.health.ArtemisHealthSupportBuildItem;
import io.quarkus.artemis.jms.ra.runtime.ConnectionFactoryHealthCheck;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ConnectorFactoryHealthCheckProcessor {
    @SuppressWarnings("unused")
    @BuildStep()
    HealthBuildItem healthChecks(
            Capabilities capabilities,
            Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (!capabilities.isPresent(Capability.SMALLRYE_HEALTH)) {
            return null;
        }
        if (artemisHealthSupportBuildItem.isEmpty()) {
            return null;
        }
        return new HealthBuildItem(ConnectionFactoryHealthCheck.class.getCanonicalName(), true);
    }
}
