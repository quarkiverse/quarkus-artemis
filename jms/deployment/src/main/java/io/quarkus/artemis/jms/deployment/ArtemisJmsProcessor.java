package io.quarkus.artemis.jms.deployment;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisCoreProcessor;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class ArtemisJmsProcessor {

    private static final String FEATURE = "artemis-jms";

    @SuppressWarnings("unused")
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @SuppressWarnings("unused")
    @BuildStep
    void load(BuildProducer<ArtemisJmsBuildItem> artemisJms) {
        artemisJms.produce(new ArtemisJmsBuildItem());
    }

    @SuppressWarnings("unused")
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisJmsConfiguredBuildItem configure(
            ArtemisJmsRecorder recorder,
            ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisBootstrappedBuildItem bootstrap,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArtemisJmsWrapperBuildItem> wrapperItem,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        ArtemisJmsWrapper wrapper = getWrapper(recorder, wrapperItem);
        final Set<String> connectionNames = bootstrap.getConnectionNames();
        recorder.init(runtimeConfigs, buildTimeConfigs, new HashSet<>(connectionNames));
        for (String name : connectionNames) {
            Supplier<ConnectionFactory> connectionFactorySupplier = recorder.getConnectionFactoryProducer(name, wrapper);
            syntheticBeanProducer.produce(toSyntheticBeanBuildItem(
                    name,
                    connectionFactorySupplier,
                    buildTimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisBuildTimeConfig()).isXaEnabled()));
        }
        return new ArtemisJmsConfiguredBuildItem();
    }

    private static ArtemisJmsWrapper getWrapper(
            ArtemisJmsRecorder recorder,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArtemisJmsWrapperBuildItem> wrapperItem) {
        ArtemisJmsWrapper wrapper;
        if (wrapperItem.isPresent()) {
            wrapper = wrapperItem.get().getWrapper();
        } else {
            wrapper = new ArtemisJmsWrapperBuildItem(recorder.getDefaultWrapper()).getWrapper();
        }
        return wrapper;
    }

    private static SyntheticBeanBuildItem toSyntheticBeanBuildItem(
            String name,
            Supplier<ConnectionFactory> connectionFactorySupplier,
            boolean isXaEnable) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = initializeConfigurator(isXaEnable)
                .supplier(connectionFactorySupplier)
                .scope(ApplicationScoped.class);
        return ArtemisCoreProcessor.addQualifiers(configurator, name)
                .setRuntimeInit()
                .done();
    }

    private static SyntheticBeanBuildItem.ExtendedBeanConfigurator initializeConfigurator(boolean isXaEnable) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator;
        if (isXaEnable) {
            configurator = SyntheticBeanBuildItem.configure(ActiveMQConnectionFactory.class);
            /**
             * Since {@link ActiveMQConnectionFactory} implements both {@link ConnectionFactory} and
             * {@link XAConnectionFactory},
             * even with "quarkus.artemis.xa.enabled=true" we still need to export ConnectionFactory which is used by
             * {@link io.quarkus.artemis.jms.runtime.health.ConnectionFactoryHealthCheck} for health checking.
             */
            configurator.addType(XAConnectionFactory.class);
            configurator.addType(ConnectionFactory.class);
        } else {
            configurator = SyntheticBeanBuildItem.configure(ConnectionFactory.class);
        }
        return configurator;
    }
}
