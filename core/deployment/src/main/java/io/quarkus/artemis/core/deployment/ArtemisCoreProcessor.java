package io.quarkus.artemis.core.deployment;

import java.util.*;
import java.util.function.Supplier;

import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.api.core.client.loadbalance.*;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.spi.core.remoting.ConnectorFactory;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.runtime.*;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ArtemisCoreProcessor {
    private static final Logger LOGGER = Logger.getLogger(ArtemisCoreProcessor.class);
    private static final String FEATURE = "artemis-core";

    static final Class<?>[] BUILTIN_CONNECTOR_FACTORIES = {
            NettyConnectorFactory.class
    };

    static final Class<?>[] BUILTIN_LOADBALANCING_POLICIES = {
            FirstElementConnectionLoadBalancingPolicy.class,
            RandomConnectionLoadBalancingPolicy.class,
            RandomStickyConnectionLoadBalancingPolicy.class,
            RoundRobinConnectionLoadBalancingPolicy.class
    };

    @SuppressWarnings("unused")
    @BuildStep
    FeatureBuildItem feature(Optional<ArtemisJmsBuildItem> artemisJms) {
        if (artemisJms.isEmpty()) {
            return new FeatureBuildItem(FEATURE);
        }
        return null;
    }

    @SuppressWarnings("unused")
    @BuildStep
    NativeImageConfigBuildItem config() {
        return NativeImageConfigBuildItem.builder()
                .addRuntimeInitializedClass(ActiveMQBuffers.class.getCanonicalName())
                .addRuntimeInitializedClass(RandomUtil.class.getCanonicalName())
                .build();
    }

    @SuppressWarnings("unused")
    @BuildStep
    ArtemisBootstrappedBuildItem build(
            CombinedIndexBuildItem indexBuildItem,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            ShadowRunTimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Collection<ClassInfo> connectorFactories = indexBuildItem.getIndex()
                .getAllKnownImplementors(DotName.createSimple(ConnectorFactory.class.getName()));
        addDynamicReflectiveBuildItems(reflectiveClass, connectorFactories);
        addBuiltinReflectiveBuildItems(reflectiveClass, BUILTIN_CONNECTOR_FACTORIES);

        Collection<ClassInfo> loadBalancers = indexBuildItem.getIndex()
                .getAllKnownImplementors(DotName.createSimple(ConnectionLoadBalancingPolicy.class.getName()));
        addDynamicReflectiveBuildItems(reflectiveClass, loadBalancers);
        addBuiltinReflectiveBuildItems(reflectiveClass, BUILTIN_LOADBALANCING_POLICIES);
        HashSet<String> names = new HashSet<>(shadowRunTimeConfigs.getNames());
        HashSet<String> disabled = new HashSet<>();
        for (var entry : buildTimeConfigs.getAllConfigs().entrySet()) {
            if (entry.getValue().isDisabled()) {
                disabled.add(entry.getKey());
            }
        }
        names.addAll(buildTimeConfigs.getAllConfigs().keySet());
        names.removeAll(disabled);
        return new ArtemisBootstrappedBuildItem(names);
    }

    @SuppressWarnings("unused")
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisCoreConfiguredBuildItem configure(
            ArtemisCoreRecorder recorder,
            ArtemisRuntimeConfigs runtimeConfigs,
            ShadowRunTimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisBootstrappedBuildItem bootstrap,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer,
            Optional<ArtemisJmsBuildItem> artemisJms) {
        if (artemisJms.isPresent()) {
            return null;
        }
        if (shadowRunTimeConfigs.isEmpty() && buildTimeConfigs.isEmpty()) {
            return null;
        }

        for (String name : bootstrap.getConfigurationNames()) {
            if (shadowRunTimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisRuntimeConfig()).isEmpty()
                    && buildTimeConfigs.getAllConfigs().getOrDefault(name, new ArtemisBuildTimeConfig()).isEmpty()) {
                continue;
            }
            Supplier<ServerLocator> supplier = recorder.getServerLocatorSupplier(
                    name,
                    runtimeConfigs,
                    buildTimeConfigs);
            SyntheticBeanBuildItem serverLocator = toSyntheticBeanBuildItem(name, supplier);
            syntheticBeanProducer.produce(serverLocator);
        }
        return new ArtemisCoreConfiguredBuildItem();
    }

    private static void addDynamicReflectiveBuildItems(
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            Collection<ClassInfo> connectorFactories) {
        for (ClassInfo ci : connectorFactories) {
            LOGGER.debug("Adding reflective class " + ci);
            reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, ci.toString()));
        }
    }

    private static void addBuiltinReflectiveBuildItems(
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            Class<?>[] builtinConnectorFactories) {
        for (Class<?> c : builtinConnectorFactories) {
            reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, c));
        }
    }

    private SyntheticBeanBuildItem toSyntheticBeanBuildItem(
            String name,
            Supplier<ServerLocator> supplier) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(ServerLocator.class)
                .supplier(supplier)
                .scope(ApplicationScoped.class);
        return addQualifiers(configurator, name)
                .setRuntimeInit()
                .done();
    }

    public static SyntheticBeanBuildItem.ExtendedBeanConfigurator addQualifiers(
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator,
            String name) {
        if (ArtemisUtil.isDefault(name)) {
            configurator
                    .unremovable()
                    .addQualifier().annotation(Default.class).done();
        }
        return configurator
                .addQualifier().annotation(Identifier.class).addValue("value", name).done();
    }
}
