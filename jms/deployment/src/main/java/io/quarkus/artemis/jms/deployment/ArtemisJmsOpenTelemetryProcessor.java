package io.quarkus.artemis.jms.deployment;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

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
            // Create a wrapper using the recorder
            // The wrapper function will check at runtime if OpenTelemetry is available
            Function<ConnectionFactory, Object> wrapper = cf -> {
                try {
                    // Try to get OpenTelemetry from CDI at runtime using reflection
                    var container = io.quarkus.arc.Arc.container();
                    if (container != null) {
                        // Use reflection to avoid compile-time dependency on OpenTelemetry
                        Class<?> otelClass = Class.forName("io.opentelemetry.api.OpenTelemetry");
                        var instance = container.instance(otelClass);
                        if (instance.isAvailable()) {
                            // Use reflection to create the tracing wrapper
                            Class<?> tracingFactoryClass = Class
                                    .forName("io.quarkus.artemis.jms.runtime.tracing.TracingConnectionFactory");
                            var constructor = tracingFactoryClass.getConstructor(ConnectionFactory.class, otelClass);
                            return constructor.newInstance(cf, instance.get());
                        }
                    }
                } catch (Exception e) {
                    // If OpenTelemetry is not available or any error occurs, return unwrapped
                }
                return cf;
            };

            wrapperProducer.produce(new ConnectionFactoryWrapperBuildItem(wrapper));
        }
    }
}
