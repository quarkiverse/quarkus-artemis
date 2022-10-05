package io.quarkus.artemis.core.deployment.health;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupportRecorder;
import io.quarkus.artemis.core.runtime.health.ServerLocatorHealthCheck;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ArtemisHealthProcessor {
    @SuppressWarnings("unused")
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisHealthSupportBuildItem healthSupport(
            Capabilities capabilities,
            ArtemisBootstrappedBuildItem bootstrap,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer,
            ArtemisHealthSupportRecorder recorder) {
        if (capabilities.isPresent(Capability.SMALLRYE_HEALTH) && buildTimeConfigs.isHealthEnabled()) {
            syntheticBeanProducer.produce(SyntheticBeanBuildItem
                    .configure(ArtemisHealthSupport.class)
                    .supplier(recorder.getArtemisSupportBuilder(bootstrap.getConnectionNames(), buildTimeConfigs))
                    .scope(ApplicationScoped.class)
                    .defaultBean()
                    .setRuntimeInit()
                    .done());
            return new ArtemisHealthSupportBuildItem();
        }
        return null;
    }

    @SuppressWarnings("unused")
    @BuildStep
    HealthBuildItem healthChecks(
            Capabilities capabilities,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArtemisJmsBuildItem> artemisJms,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (artemisJms.isEmpty()
                && capabilities.isPresent(Capability.SMALLRYE_HEALTH)
                && artemisHealthSupportBuildItem.isPresent()) {
            return new HealthBuildItem(ServerLocatorHealthCheck.class.getCanonicalName(), true);
        }
        return null;
    }
}
