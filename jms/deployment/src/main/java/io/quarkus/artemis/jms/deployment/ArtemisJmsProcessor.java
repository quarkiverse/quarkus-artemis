package io.quarkus.artemis.jms.deployment;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
import io.quarkus.artemis.jms.runtime.ConnectionFactoryWrapper;
import io.quarkus.core.Phase;
import io.quarkus.core.deployment.service.ServiceRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.OpenTelemetrySdkBuildItem;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ArtemisJmsProcessor {

    private static final String FEATURE = "artemis-jms";
    private static final String WRAPPER_SERVICE_PREFIX = "io.quarkus.artemis.jms.connection-factory-wrapper";
    private static final String WRAPPER_IDENTITY_SERVICE = WRAPPER_SERVICE_PREFIX + ".identity";

    @SuppressWarnings("unused")
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void load(BuildProducer<ArtemisJmsBuildItem> artemisJms) {
        artemisJms.produce(new ArtemisJmsBuildItem());
    }

    @BuildStep
    ArtemisJmsConfiguredBuildItem configure(
            ServiceRegistrar serviceRegistrar,
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs,
            ArtemisBootstrappedBuildItem bootstrap,
            Optional<io.quarkiverse.jms.spi.deployment.ConnectionFactoryWrapperBuildItem> externalWrapper,
            List<ConnectionFactoryWrapperBuildItem> localWrappers,
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        if (shadowRunTimeConfigs.isEmpty() && buildTimeConfigs.isEmpty()) {
            return new ArtemisJmsConfiguredBuildItem();
        }

        boolean otelEnabled = ArtemisJmsOpenTelemetryProcessor
                .isTracingWrapperEnabled(openTelemetrySdkBuildItem);
        String wrapperService = registerConnectionFactoryWrappers(serviceRegistrar, localWrappers, externalWrapper,
                otelEnabled);
        Set<String> configurationNames = bootstrap.getConfigurationNames();
        boolean isSoleConnectionFactory = configurationNames.size() == 1;
        for (String name : configurationNames) {
            if (!shadowRunTimeConfigs.getNames().contains(name) && buildTimeConfigs.configs().get(name).isEmpty()) {
                continue;
            }
            serviceRegistrar.forService(ConnectionFactory.class, name)
                    .atPhase(Phase.DATA)
                    .require(ArtemisRuntimeConfigs.class)
                    .require(ConnectionFactoryWrapper.class, wrapperService)
                    .onStart((ctx, runtimeConfigs, wrapper) -> ArtemisJmsRecorder.createConnectionFactory(
                            name, runtimeConfigs, buildTimeConfigs, wrapper));
            syntheticBeanProducer.produce(toSyntheticBeanBuildItem(
                    name,
                    isSoleConnectionFactory,
                    buildTimeConfigs.configs().get(name).isXaEnabled()));
        }
        return new ArtemisJmsConfiguredBuildItem();
    }

    private static String registerConnectionFactoryWrappers(
            ServiceRegistrar serviceRegistrar,
            List<ConnectionFactoryWrapperBuildItem> localWrappers,
            Optional<io.quarkiverse.jms.spi.deployment.ConnectionFactoryWrapperBuildItem> externalWrapper,
            boolean otelEnabled) {
        String previous = WRAPPER_IDENTITY_SERVICE;
        serviceRegistrar.forService(ConnectionFactoryWrapper.class, previous)
                .atPhase(Phase.DATA)
                .onStart(ctx -> ArtemisJmsRecorder.getDefaultWrapper());

        int index = 0;
        if (otelEnabled) {
            previous = composeWrapper(serviceRegistrar, previous,
                    ArtemisJmsOpenTelemetryProcessor.OTEL_WRAPPER_SERVICE, index++);
        }

        // Compose all locally contributed wrappers so they wrap the raw ConnectionFactory.
        for (ConnectionFactoryWrapperBuildItem localWrapper : localWrappers) {
            previous = wrapRecorderValue(serviceRegistrar, previous, localWrapper.getWrapper(), index++);
        }

        // Then apply the external wrapper (e.g., from quarkus-pooled-jms) on top.
        // This ensures the CDI bean type is preserved (e.g., DelegatingJmsPoolConnectionFactory)
        // so that downstream initializers can detect it via instanceof checks.
        if (externalWrapper.isPresent()) {
            previous = wrapRecorderValue(serviceRegistrar, previous, externalWrapper.get().getWrapper(), index);
        }

        return previous;
    }

    /**
     * Bridges a recorder-produced {@link Function} into the service graph. The raw function is
     * aliased as an {@link Object} service (because {@link Function} is generic and therefore
     * cannot be a service type) and then adapted to a {@link ConnectionFactoryWrapper}.
     * <p>
     * {@code aliasRecorderValue} is deprecated together with the recorder system, but it is
     * still the only bridge for values that recorder-based extensions pass through build items,
     * such as the wrapper contributed by quarkus-pooled-jms. This path can be removed once those
     * extensions expose their wrappers as services.
     */
    @SuppressWarnings("unchecked")
    private static String wrapRecorderValue(
            ServiceRegistrar serviceRegistrar,
            String previous,
            Function<ConnectionFactory, Object> wrapper,
            int index) {
        String alias = WRAPPER_SERVICE_PREFIX + ".raw." + index;
        serviceRegistrar.aliasRecorderValue(Object.class, alias, wrapper, Phase.DATA);
        String adapted = WRAPPER_SERVICE_PREFIX + ".adapted." + index;
        serviceRegistrar.forService(ConnectionFactoryWrapper.class, adapted)
                .atPhase(Phase.DATA)
                .require(Object.class, alias)
                .onStart((ctx, raw) -> ((Function<ConnectionFactory, Object>) raw)::apply);
        return composeWrapper(serviceRegistrar, previous, adapted, index);
    }

    private static String composeWrapper(ServiceRegistrar serviceRegistrar, String previous, String next, int index) {
        String composed = WRAPPER_SERVICE_PREFIX + ".composed." + index;
        serviceRegistrar.forService(ConnectionFactoryWrapper.class, composed)
                .atPhase(Phase.DATA)
                .require(ConnectionFactoryWrapper.class, previous)
                .require(ConnectionFactoryWrapper.class, next)
                .onStart((ctx, previousWrapper, nextWrapper) -> ArtemisJmsRecorder
                        .composeWrappers(previousWrapper, nextWrapper));
        return composed;
    }

    private static SyntheticBeanBuildItem toSyntheticBeanBuildItem(
            String name,
            boolean isSoleConnectionFactory,
            boolean isXaEnable) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = initializeConfigurator(isXaEnable)
                .serviceValue(ConnectionFactory.class, name)
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
