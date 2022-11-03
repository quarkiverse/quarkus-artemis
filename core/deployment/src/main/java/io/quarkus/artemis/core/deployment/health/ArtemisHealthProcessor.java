package io.quarkus.artemis.core.deployment.health;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.deployment.ShadowRunTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
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
    @Record(ExecutionTime.STATIC_INIT)
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
        Set<String> names = bootstrap.getConfigurationNames();
        Set<String> excludedNames = processConfigs(names, shadowRunTimeConfigs, buildTimeConfigs);
        syntheticBeanProducer.produce(SyntheticBeanBuildItem
                .configure(ArtemisHealthSupport.class)
                .supplier(recorder.getArtemisSupportBuilder(names, excludedNames))
                .scope(ApplicationScoped.class)
                .defaultBean()
                .done());
        return new ArtemisHealthSupportBuildItem();
    }

    private static Set<String> processConfigs(
            Set<String> names,
            ShadowRunTimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Set<String> excluded = new HashSet<>();
        Map<String, ArtemisBuildTimeConfig> allBuildTimeConfigs = Optional.ofNullable(buildTimeConfigs.getAllConfigs())
                .orElse(Map.of());
        for (String name : names) {
            ArtemisBuildTimeConfig buildTimeConfig = allBuildTimeConfigs.getOrDefault(name, new ArtemisBuildTimeConfig());
            if ((ArtemisUtil.isDefault(name) && !shadowRunTimeConfigs.getNames().contains(name) && buildTimeConfig.isEmpty())
                    || buildTimeConfig.isHealthExclude()) {
                excluded.add(name);
            }
        }
        return excluded;
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
