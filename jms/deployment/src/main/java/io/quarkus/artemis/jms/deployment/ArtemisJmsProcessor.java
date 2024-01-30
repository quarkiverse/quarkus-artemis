package io.quarkus.artemis.jms.deployment;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.XAConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBootstrappedBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisCoreProcessor;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.core.deployment.ShadowRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisBootstrappedBuildItem bootstrap,
            Optional<ConnectionFactoryWrapperBuildItem> wrapperItem,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        if (shadowRunTimeConfigs.isEmpty() && buildTimeConfigs.isEmpty()) {
            return new ArtemisJmsConfiguredBuildItem();
        }

        Function<ConnectionFactory, Object> wrapper = getWrapper(recorder, wrapperItem);
        Set<String> configurationNames = bootstrap.getConfigurationNames();
        boolean isSoleConnectionFactory = configurationNames.size() == 1;
        for (String name : configurationNames) {
            if (!shadowRunTimeConfigs.getNames().contains(name) && buildTimeConfigs.configs().get(name).isEmpty()) {
                continue;
            }
            Supplier<ConnectionFactory> connectionFactorySupplier = recorder.getConnectionFactoryProducer(
                    name,
                    runtimeConfigs,
                    buildTimeConfigs,
                    wrapper);
            syntheticBeanProducer.produce(toSyntheticBeanBuildItem(
                    connectionFactorySupplier,
                    name,
                    isSoleConnectionFactory,
                    buildTimeConfigs.configs().get(name).isXaEnabled()));
        }
        return new ArtemisJmsConfiguredBuildItem();
    }

    private static Function<ConnectionFactory, Object> getWrapper(
            ArtemisJmsRecorder recorder,
            Optional<ConnectionFactoryWrapperBuildItem> wrapperItem) {
        Function<ConnectionFactory, Object> wrapper;
        if (wrapperItem.isPresent()) {
            wrapper = wrapperItem.get().getWrapper();
        } else {
            wrapper = recorder.getDefaultWrapper();
        }
        return wrapper;
    }

    private static SyntheticBeanBuildItem toSyntheticBeanBuildItem(
            Supplier<ConnectionFactory> connectionFactorySupplier,
            String name,
            boolean isSoleConnectionFactory,
            boolean isXaEnable) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = initializeConfigurator(isXaEnable)
                .supplier(connectionFactorySupplier)
                .scope(ApplicationScoped.class)
                .name(name);
        return ArtemisCoreProcessor.addQualifiers(name, isSoleConnectionFactory, configurator)
                .setRuntimeInit()
                .done();
    }

    private static SyntheticBeanBuildItem.ExtendedBeanConfigurator initializeConfigurator(boolean isXaEnable) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator;
        if (isXaEnable) {
            configurator = SyntheticBeanBuildItem.configure(ActiveMQXAConnectionFactory.class);
            configurator.addType(ActiveMQConnectionFactory.class);
            /*
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
