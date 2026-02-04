package io.quarkus.artemis.jms.deployment;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;

/**
 * Processor for adding OpenTelemetry tracing to Artemis JMS.
 * Uses the local ConnectionFactoryWrapperBuildItem (MultiBuildItem) to allow
 * composition with other wrappers like quarkus-pooled-jms.
 */
public class ArtemisJmsOpenTelemetryProcessor {

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerTracingWrapper(
            ArtemisJmsRecorder recorder,
            Capabilities capabilities,
            BuildProducer<ConnectionFactoryWrapperBuildItem> wrapperProducer,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        // Only enable if OpenTelemetry is present
        if (capabilities.isPresent(Capability.OPENTELEMETRY_TRACER)) {
            // Use the recorder to create the wrapper at runtime
            // This uses the local MultiBuildItem, not the external SimpleBuildItem
            wrapperProducer.produce(new ConnectionFactoryWrapperBuildItem(recorder.getOpenTelemetryWrapper()));
        }
    }
}
