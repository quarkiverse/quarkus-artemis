package io.quarkus.artemis.core.deployment.health;

import java.util.Optional;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ShadowRunTimeConfigs;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupportRecorder;
import io.quarkus.artemis.core.runtime.health.ServerLocatorHealthCheck;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ArtemisHealthProcessor {
    @SuppressWarnings("unused")
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisHealthSupportBuildItem healthSupport(
            Capabilities capabilities,
            ArtemisBootstrappedBuildItem bootstrap,
            ShadowRunTimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer,
            ArtemisHealthSupportRecorder recorder) {
        if (!buildTimeConfigs.isHealthEnabled()) {
            return null;
        }
        syntheticBeanProducer.produce(SyntheticBeanBuildItem
                .configure(ArtemisHealthSupport.class)
                .supplier(recorder.getArtemisSupportBuilder(bootstrap.getConfigurationNames(), shadowRunTimeConfigs,
                        buildTimeConfigs))
                .scope(ApplicationScoped.class)
                .defaultBean()
                .setRuntimeInit()
                .done());
        return new ArtemisHealthSupportBuildItem();
    }

    @SuppressWarnings("unused")
    @BuildStep
    HealthBuildItem healthChecks(
            Capabilities capabilities,
            Optional<ArtemisJmsBuildItem> artemisJms,
            Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (artemisJms.isPresent()) {
            return null;
        }
        if (artemisHealthSupportBuildItem.isEmpty()) {
            return null;
        }
        return new HealthBuildItem(ServerLocatorHealthCheck.class.getCanonicalName(), true);
    }
}
