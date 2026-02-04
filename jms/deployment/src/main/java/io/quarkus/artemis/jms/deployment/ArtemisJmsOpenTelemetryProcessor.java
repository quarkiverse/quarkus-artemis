package io.quarkus.artemis.jms.deployment;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem;

/**
 * Processor for adding OpenTelemetry tracing to Artemis JMS.
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
            // Create a synthetic bean for OpenTelemetry if needed
            // The OpenTelemetry bean should already be provided by quarkus-opentelemetry extension
            
            // Create a wrapper using the recorder
            // Note: We can't directly access OpenTelemetry at build time, 
            // so we need to use a runtime value approach
            // For now, let's use a simpler approach - check at runtime if OpenTelemetry is available
            Function<ConnectionFactory, Object> wrapper = cf -> {
                try {
                    // Try to get OpenTelemetry from CDI at runtime
                    var container = io.quarkus.arc.Arc.container();
                    if (container != null) {
                        var instance = container.instance(OpenTelemetry.class);
                        if (instance.isAvailable()) {
                            return recorder.getTracingWrapper(instance.get()).apply(cf);
                        }
                    }
                } catch (Exception e) {
                    // If OpenTelemetry is not available, return unwrapped
                }
                return cf;
            };

            wrapperProducer.produce(new ConnectionFactoryWrapperBuildItem(wrapper));
        }
    }
}
