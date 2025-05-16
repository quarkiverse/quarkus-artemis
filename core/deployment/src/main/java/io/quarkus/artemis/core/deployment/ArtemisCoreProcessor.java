package io.quarkus.artemis.core.deployment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.api.core.client.loadbalance.ConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.api.core.client.loadbalance.FirstElementConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.api.core.client.loadbalance.RandomConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.api.core.client.loadbalance.RandomStickyConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.core.protocol.hornetq.client.HornetQClientProtocolManagerFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.remoting.ConnectorFactory;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisCoreRecorder;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.smallrye.common.annotation.Identifier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ArtemisCoreProcessor {
    private static final Logger LOGGER = Logger.getLogger(ArtemisCoreProcessor.class);
    private static final String FEATURE = "artemis-core";

    static final List<ReflectiveClassBuildItem> BUILTIN_CONNECTOR_FACTORIES_REFLECTION_CONFIG = List.of(
            ReflectiveClassBuildItem
                    .builder(NettyConnectorFactory.class)
                    .fields(false)
                    .methods(false)
                    .build(),
            ReflectiveClassBuildItem
                    .builder(ActiveMQConnectionFactory.class)
                    .fields(false)
                    .methods(true)
                    .build(),
            ReflectiveClassBuildItem
                    .builder(HornetQClientProtocolManagerFactory.class)
                    .fields(false)
                    .methods(false)
                    .build());

    static final List<ReflectiveClassBuildItem> BUILTIN_LOADBALANCING_POLICIES_REFLECTION_CONFIG = List.of(
            ReflectiveClassBuildItem
                    .builder(FirstElementConnectionLoadBalancingPolicy.class)
                    .fields(false)
                    .methods(false)
                    .build(),
            ReflectiveClassBuildItem
                    .builder(RandomConnectionLoadBalancingPolicy.class)
                    .fields(false)
                    .methods(false)
                    .build(),
            ReflectiveClassBuildItem
                    .builder(RandomStickyConnectionLoadBalancingPolicy.class)
                    .fields(false)
                    .methods(false)
                    .build(),
            ReflectiveClassBuildItem
                    .builder(RoundRobinConnectionLoadBalancingPolicy.class)
                    .fields(false)
                    .methods(false)
                    .build());

    @SuppressWarnings("unused")
    @BuildStep
    FeatureBuildItem feature(Optional<ArtemisJmsBuildItem> artemisJms,
            Optional<ArtemisJmsRABuildItem> ra) {
        if (artemisJms.isEmpty() && ra.isEmpty()) {
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
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        Collection<ClassInfo> connectorFactories = indexBuildItem.getIndex()
                .getAllKnownImplementations(DotName.createSimple(ConnectorFactory.class.getName()));

        addDynamicReflectiveBuildItems(reflectiveClass, connectorFactories);
        BUILTIN_CONNECTOR_FACTORIES_REFLECTION_CONFIG.forEach(reflectiveClass::produce);

        Collection<ClassInfo> loadBalancers = indexBuildItem.getIndex()
                .getAllKnownImplementations(DotName.createSimple(ConnectionLoadBalancingPolicy.class.getName()));
        addDynamicReflectiveBuildItems(reflectiveClass, loadBalancers);
        BUILTIN_LOADBALANCING_POLICIES_REFLECTION_CONFIG.forEach(reflectiveClass::produce);
        HashSet<String> names = new HashSet<>(shadowRunTimeConfigs.getNames());
        HashSet<String> disabled = new HashSet<>();
        for (var entry : buildTimeConfigs.configs().entrySet()) {
            if (entry.getValue().isDisabled()) {
                disabled.add(entry.getKey());
            }
        }
        names.addAll(buildTimeConfigs.getNames());
        names.removeAll(disabled);
        return new ArtemisBootstrappedBuildItem(names);
    }

    @SuppressWarnings("unused")
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisCoreConfiguredBuildItem configure(
            ArtemisCoreRecorder recorder,
            ArtemisRuntimeConfigs runtimeConfigs,
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisBootstrappedBuildItem bootstrap,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer,
            Optional<ArtemisJmsBuildItem> artemisJms,
            Optional<ArtemisJmsRABuildItem> ra) {
        if (artemisJms.isPresent() || ra.isPresent()) {
            return null;
        }
        if (shadowRunTimeConfigs.isEmpty() && buildTimeConfigs.isEmpty()) {
            return new ArtemisCoreConfiguredBuildItem();
        }

        boolean isSoleServerLocator = bootstrap.getConfigurationNames().size() == 1;
        for (String name : bootstrap.getConfigurationNames()) {
            if (!shadowRunTimeConfigs.getNames().contains(name)
                    && buildTimeConfigs.configs().get(name).isEmpty()) {
                continue;
            }
            Supplier<ServerLocator> supplier = recorder.getServerLocatorSupplier(
                    name,
                    runtimeConfigs,
                    buildTimeConfigs);
            SyntheticBeanBuildItem serverLocator = toSyntheticBeanBuildItem(supplier, name, isSoleServerLocator);
            syntheticBeanProducer.produce(serverLocator);
        }
        return new ArtemisCoreConfiguredBuildItem();
    }

    private static void addDynamicReflectiveBuildItems(
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            Collection<ClassInfo> connectorFactories) {
        for (ClassInfo ci : connectorFactories) {
            LOGGER.debug("Adding reflective class " + ci);
            reflectiveClass.produce(ReflectiveClassBuildItem
                    .builder(ci.toString())
                    .methods(false)
                    .fields(false)
                    .build());
        }
    }

    private SyntheticBeanBuildItem toSyntheticBeanBuildItem(
            Supplier<ServerLocator> supplier,
            String name,
            boolean isSoleServerLocator) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(ServerLocator.class)
                .supplier(supplier)
                .scope(ApplicationScoped.class);
        return addQualifiers(name, isSoleServerLocator, configurator)
                .setRuntimeInit()
                .done();
    }

    public static SyntheticBeanBuildItem.ExtendedBeanConfigurator addQualifiers(
            String name,
            boolean isSoleArtemisBean,
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator) {
        if (ArtemisUtil.isDefault(name) || isSoleArtemisBean) {
            configurator
                    .unremovable()
                    .addQualifier().annotation(Default.class).done()
                    .name(name);
        }
        return configurator
                .addQualifier().annotation(Identifier.class).addValue("value", name).done();
    }
}
