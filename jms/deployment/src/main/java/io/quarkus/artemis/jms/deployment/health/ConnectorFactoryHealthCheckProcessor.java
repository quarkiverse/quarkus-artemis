package io.quarkus.artemis.jms.deployment.health;

import io.quarkus.artemis.core.deployment.health.ArtemisHealthSupportBuildItem;
import io.quarkus.artemis.jms.runtime.health.ConnectionFactoryHealthCheck;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ConnectorFactoryHealthCheckProcessor {
    @BuildStep()
    HealthBuildItem healthChecks(ArtemisHealthSupportBuildItem artemisHealthSupportBuildItem) {
        return new HealthBuildItem(ConnectionFactoryHealthCheck.class.getCanonicalName(), true);
    }
}
