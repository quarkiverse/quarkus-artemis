package io.quarkus.artemis.jms.ra.deployment;

import java.util.Optional;

import io.quarkus.artemis.core.deployment.ArtemisJmsRABuildItem;
import io.quarkus.artemis.core.deployment.health.ArtemisHealthSupportBuildItem;
import io.quarkus.artemis.jms.ra.runtime.ConnectionFactoryHealthCheck;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ArtemisResourceAdapterProcessor {
    private static final String FEATURE = "artemis-jms-ra";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void load(BuildProducer<ArtemisJmsRABuildItem> ra) {
        ra.produce(new ArtemisJmsRABuildItem());
    }

    @BuildStep
    HealthBuildItem healthChecks(
            Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (artemisHealthSupportBuildItem.isEmpty()) {
            return null;
        }
        return new HealthBuildItem(ConnectionFactoryHealthCheck.class.getCanonicalName(), true);
    }
}
