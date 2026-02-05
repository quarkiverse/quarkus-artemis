package io.quarkus.artemis.jms.deployment;

import java.util.Optional;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.OpenTelemetrySdkBuildItem;
import io.quarkus.artemis.jms.runtime.ArtemisJmsOpenTelemetryWrapper;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.runtime.RuntimeValue;

/**
 * Processor for adding OpenTelemetry tracing to Artemis JMS.
 * Uses the local ConnectionFactoryWrapperBuildItem (MultiBuildItem) to allow
 * composition with other wrappers like quarkus-pooled-jms.
 */
public class ArtemisJmsOpenTelemetryProcessor {

    @BuildStep
    void registerOpenTelemetryWrapper(
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Only register the wrapper bean if OpenTelemetry SDK is present and tracing is enabled
        // OpenTelemetrySdkBuildItem is only produced when OpenTelemetry is enabled (not just present)
        // This properly handles the case when OpenTelemetry is disabled via config (e.g., quarkus.otel.sdk.disabled=true)
        if (openTelemetrySdkBuildItem.isPresent() && openTelemetrySdkBuildItem.get().isTracingBuildTimeEnabled()) {
            // Register the OpenTelemetry wrapper as a CDI bean
            additionalBeans.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(ArtemisJmsOpenTelemetryWrapper.class)
                    .setUnremovable()
                    .build());
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerTracingWrapper(
            ArtemisJmsRecorder recorder,
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem,
            BuildProducer<ConnectionFactoryWrapperBuildItem> wrapperProducer) {

        // Only enable if OpenTelemetry SDK is present and tracing is enabled
        if (openTelemetrySdkBuildItem.isPresent() && openTelemetrySdkBuildItem.get().isTracingBuildTimeEnabled()) {
            // Get the runtime enabled flag to check at runtime if OpenTelemetry is active
            Optional<RuntimeValue<Boolean>> runtimeEnabled = OpenTelemetrySdkBuildItem
                    .isOtelSdkEnabled(openTelemetrySdkBuildItem);

            // Use the recorder to create the wrapper at runtime
            // This uses the local MultiBuildItem, not the external SimpleBuildItem
            wrapperProducer.produce(new ConnectionFactoryWrapperBuildItem(recorder.getOpenTelemetryWrapper(runtimeEnabled)));
        }
    }
}
