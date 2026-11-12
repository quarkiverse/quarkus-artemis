package io.quarkus.artemis.jms.deployment;

import java.util.Optional;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.artemis.jms.runtime.ArtemisJmsOpenTelemetryWrapper;
import io.quarkus.artemis.jms.runtime.ConnectionFactoryWrapper;
import io.quarkus.core.Phase;
import io.quarkus.core.deployment.service.ServiceRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.OpenTelemetrySdkBuildItem;

/**
 * Processor for adding OpenTelemetry tracing to Artemis JMS.
 * The tracing wrapper is registered as a service so it can be composed with
 * other connection factory wrappers like quarkus-pooled-jms.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ArtemisJmsOpenTelemetryProcessor {

    static final String OTEL_WRAPPER_SERVICE = "io.quarkus.artemis.jms.connection-factory-wrapper.otel";

    @BuildStep
    void registerOpenTelemetryWrapper(
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Only register the wrapper bean if OpenTelemetry SDK is present and tracing is enabled.
        // OpenTelemetrySdkBuildItem is only produced when OpenTelemetry is enabled at build time
        // (quarkus.otel.enabled and quarkus.otel.traces.enabled).
        if (isTracingWrapperEnabled(openTelemetrySdkBuildItem)) {
            // Register the OpenTelemetry wrapper as a CDI bean
            additionalBeans.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(ArtemisJmsOpenTelemetryWrapper.class)
                    .setUnremovable()
                    .build());
        }
    }

    @BuildStep
    void registerTracingWrapper(
            ServiceRegistrar serviceRegistrar,
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem) {

        // Only enable if OpenTelemetry SDK is present and tracing is enabled
        if (!isTracingWrapperEnabled(openTelemetrySdkBuildItem)) {
            return;
        }

        // The runtime enabled flag is read inside the runtime wrapper, so this deployment
        // module does not reference the optional OpenTelemetry runtime config type.
        serviceRegistrar.forService(ConnectionFactoryWrapper.class, OTEL_WRAPPER_SERVICE)
                .atPhase(Phase.DATA)
                .onStart(ctx -> ArtemisJmsOpenTelemetryWrapper.createWrapper());
    }

    static boolean isTracingWrapperEnabled(Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem) {
        return openTelemetrySdkBuildItem.isPresent()
                && openTelemetrySdkBuildItem.get().isTracingBuildTimeEnabled();
    }
}
