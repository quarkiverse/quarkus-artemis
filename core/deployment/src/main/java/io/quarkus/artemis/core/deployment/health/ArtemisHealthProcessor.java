package io.quarkus.artemis.core.deployment.health;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsRABuildItem;
import io.quarkus.artemis.core.deployment.ShadowRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupport;
import io.quarkus.artemis.core.runtime.health.ArtemisHealthSupportRecorder;
import io.quarkus.artemis.core.runtime.health.ServerLocatorHealthCheck;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ArtemisHealthProcessor {
    @SuppressWarnings("unused")
    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    ArtemisHealthSupportBuildItem healthSupport(
            Capabilities capabilities,
            ArtemisBootstrappedBuildItem bootstrap,
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer,
            List<ExtraArtemisHealthCheckBuildItem> extras,
            ArtemisHealthSupportRecorder recorder) {
        if (!buildTimeConfigs.isHealthEnabled()) {
            return null;
        }
        Set<String> names = Stream
                .concat(
                        bootstrap.getConfigurationNames().stream(),
                        extras.stream().map(ExtraArtemisHealthCheckBuildItem::getName))
                .collect(Collectors.toSet());
        syntheticBeanProducer.produce(SyntheticBeanBuildItem
                .configure(ArtemisHealthSupport.class)
                .supplier(recorder.getArtemisHealthSupportBuilder(names))
                .scope(ApplicationScoped.class)
                .defaultBean()
                .done());
        return new ArtemisHealthSupportBuildItem();
    }

    @SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "unused" })
    @BuildStep
    HealthBuildItem healthChecks(
            Capabilities capabilities,
            Optional<ArtemisJmsBuildItem> artemisJms,
            Optional<ArtemisJmsRABuildItem> ra,
            Optional<ArtemisHealthSupportBuildItem> artemisHealthSupportBuildItem) {
        if (artemisJms.isPresent() || ra.isPresent()) {
            return null;
        }
        if (artemisHealthSupportBuildItem.isEmpty()) {
            return null;
        }
        return new HealthBuildItem(ServerLocatorHealthCheck.class.getCanonicalName(), true);
    }
}
