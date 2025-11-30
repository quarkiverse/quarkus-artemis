package io.quarkus.artemis.jms.ra.deployment;

import io.quarkus.artemis.core.deployment.ArtemisJmsRABuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

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
}
