package io.quarkus.artemis.jms.runtime;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {
    private final ArtemisBuildTimeConfigs buildTimeConfigs;
    private final RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs;

    public ArtemisJmsRecorder(ArtemisBuildTimeConfigs buildTimeConfigs, RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs) {
        this.buildTimeConfigs = buildTimeConfigs;
        this.runtimeConfigs = runtimeConfigs;
    }

    public Function<ConnectionFactory, Object> getDefaultWrapper() {
        return cf -> cf;
    }

    /**
     * Composes multiple ConnectionFactory wrappers into a single wrapper.
     * Wrappers are applied in order: wrapper2(wrapper1(cf))
     *
     * @param wrapper1 the first wrapper to apply
     * @param wrapper2 the second wrapper to apply on top of the first
     * @return a composed wrapper function
     */
    public Function<ConnectionFactory, Object> composeWrappers(
            Function<ConnectionFactory, Object> wrapper1,
            Function<ConnectionFactory, Object> wrapper2) {
        return cf -> {
            Object wrapped = wrapper1.apply(cf);
            if (wrapped instanceof ConnectionFactory) {
                return wrapper2.apply((ConnectionFactory) wrapped);
            }
            return wrapped;
        };
    }

    public Supplier<ConnectionFactory> getConnectionFactoryProducer(String name, Function<ConnectionFactory, Object> wrapper) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getValue().configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        final ConnectionFactory connectionFactory = Objects
                .requireNonNull(extractConnectionFactory(buildTimeConfig.isXaEnabled(), runtimeConfig, wrapper));
        return () -> connectionFactory;
    }

    private ConnectionFactory extractConnectionFactory(boolean isXaEnabled, ArtemisRuntimeConfig runtimeConfig,
            Function<ConnectionFactory, Object> wrapper) {
        String url = runtimeConfig.getUrl();
        if (url != null) {
            if (isXaEnabled) {
                return (ConnectionFactory) wrapper.apply(applyConfig(
                        new ActiveMQXAConnectionFactory(
                                url,
                                runtimeConfig.getUsername(),
                                runtimeConfig.getPassword()),
                        runtimeConfig));
            } else {
                return (ConnectionFactory) wrapper.apply(applyConfig(
                        new ActiveMQConnectionFactory(
                                url,
                                runtimeConfig.getUsername(),
                                runtimeConfig.getPassword()),
                        runtimeConfig));
            }
        } else {
            return null;
        }
    }

    private static <T extends ActiveMQConnectionFactory> T applyConfig(T connectionFactory,
            ArtemisRuntimeConfig runtimeConfig) {
        // --- Flow Control ---
        runtimeConfig.consumerWindowSize().ifPresent(connectionFactory::setConsumerWindowSize);
        runtimeConfig.consumerMaxRate().ifPresent(connectionFactory::setConsumerMaxRate);
        runtimeConfig.producerWindowSize().ifPresent(connectionFactory::setProducerWindowSize);
        runtimeConfig.producerMaxRate().ifPresent(connectionFactory::setProducerMaxRate);
        runtimeConfig.confirmationWindowSize().ifPresent(connectionFactory::setConfirmationWindowSize);
        runtimeConfig.minLargeMessageSize().ifPresent(connectionFactory::setMinLargeMessageSize);
        runtimeConfig.compressLargeMessage().ifPresent(connectionFactory::setCompressLargeMessage);
        runtimeConfig.compressionLevel().ifPresent(connectionFactory::setCompressionLevel);
        runtimeConfig.cacheLargeMessagesClient().ifPresent(connectionFactory::setCacheLargeMessagesClient);

        // --- Connectivity & Timeouts ---
        runtimeConfig.clientFailureCheckPeriod().ifPresent(connectionFactory::setClientFailureCheckPeriod);
        runtimeConfig.connectionTtl().ifPresent(connectionFactory::setConnectionTTL);
        runtimeConfig.callTimeout().ifPresent(connectionFactory::setCallTimeout);
        runtimeConfig.callFailoverTimeout().ifPresent(connectionFactory::setCallFailoverTimeout);
        runtimeConfig.useGlobalPools().ifPresent(connectionFactory::setUseGlobalPools);
        runtimeConfig.scheduledThreadPoolMaxSize().ifPresent(connectionFactory::setScheduledThreadPoolMaxSize);
        runtimeConfig.threadPoolMaxSize().ifPresent(connectionFactory::setThreadPoolMaxSize);

        // --- Retries & Failover ---
        runtimeConfig.reconnectAttempts().ifPresent(connectionFactory::setReconnectAttempts);
        runtimeConfig.initialConnectAttempts().ifPresent(connectionFactory::setInitialConnectAttempts);
        runtimeConfig.retryInterval().ifPresent(connectionFactory::setRetryInterval);
        runtimeConfig.retryIntervalMultiplier().ifPresent(connectionFactory::setRetryIntervalMultiplier);
        runtimeConfig.maxRetryInterval().ifPresent(connectionFactory::setMaxRetryInterval);
        runtimeConfig.failoverOnInitialConnection().ifPresent(connectionFactory::setFailoverOnInitialConnection);

        // --- Behavior & Grouping ---
        runtimeConfig.autoGroup().ifPresent(connectionFactory::setAutoGroup);
        runtimeConfig.groupID().ifPresent(connectionFactory::setGroupID);
        runtimeConfig.blockOnAcknowledge().ifPresent(connectionFactory::setBlockOnAcknowledge);
        runtimeConfig.blockOnDurableSend().ifPresent(connectionFactory::setBlockOnDurableSend);
        runtimeConfig.blockOnNonDurableSend().ifPresent(connectionFactory::setBlockOnNonDurableSend);
        runtimeConfig.preAcknowledge().ifPresent(connectionFactory::setPreAcknowledge);
        runtimeConfig.dupsOKBatchSize().ifPresent(connectionFactory::setDupsOKBatchSize);
        runtimeConfig.transactionBatchSize().ifPresent(connectionFactory::setTransactionBatchSize);
        runtimeConfig.cacheDestinations().ifPresent(connectionFactory::setCacheDestinations);

        // --- Security & Policy ---
        runtimeConfig.deserializationAllowList().ifPresent(connectionFactory::setDeserializationAllowList);
        runtimeConfig.deserializationDenyList().ifPresent(connectionFactory::setDeserializationDenyList);
        runtimeConfig.incomingInterceptorList().ifPresent(connectionFactory::setIncomingInterceptorList);
        runtimeConfig.outgoingInterceptorList().ifPresent(connectionFactory::setOutgoingInterceptorList);
        runtimeConfig.connectionLoadBalancingPolicyClassName()
                .ifPresent(connectionFactory::setConnectionLoadBalancingPolicyClassName);
        runtimeConfig.protocolManagerFactoryStr().ifPresent(connectionFactory::setProtocolManagerFactoryStr);

        // --- Compatibility & Identification ---
        runtimeConfig.clientID().ifPresent(connectionFactory::setClientID);
        runtimeConfig.enableSharedClientID().ifPresent(connectionFactory::setEnableSharedClientID);
        runtimeConfig.enable1xPrefixes().ifPresent(connectionFactory::setEnable1xPrefixes);
        runtimeConfig.ignoreJTA().ifPresent(connectionFactory::setIgnoreJTA);
        runtimeConfig.useTopologyForLoadBalancing().ifPresent(connectionFactory::setUseTopologyForLoadBalancing);
        runtimeConfig.initialMessagePacketSize().ifPresent(connectionFactory::setInitialMessagePacketSize);
        return connectionFactory;
    }

    /**
     * Creates a wrapper function that adds OpenTelemetry tracing to ConnectionFactory.
     * Uses reflection to avoid compile-time dependency on OpenTelemetry.
     *
     * @return a wrapper function
     */
    /**
     * Creates a wrapper function that adds OpenTelemetry tracing to ConnectionFactory.
     * Uses reflection to avoid compile-time dependency on OpenTelemetry.
     * The wrapping is done lazily to ensure OpenTelemetry beans are initialized.
     *
     * @return a wrapper function
     */
    public Function<ConnectionFactory, Object> getOpenTelemetryWrapper() {
        return cf -> {
            // Create a lazy proxy that defers OpenTelemetry lookup until first use
            return java.lang.reflect.Proxy.newProxyInstance(
                    cf.getClass().getClassLoader(),
                    new Class<?>[] { ConnectionFactory.class },
                    (proxy, method, args) -> {
                        // Lazily wrap on first method invocation
                        ConnectionFactory wrapped = getTracingConnectionFactory(cf);
                        return method.invoke(wrapped, args);
                    });
        };
    }

    private ConnectionFactory getTracingConnectionFactory(ConnectionFactory cf) {
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
                    Object wrapped = constructor.newInstance(cf, instance.get());
                    return (ConnectionFactory) wrapped;
                }
            }
        } catch (Exception e) {
            // If OpenTelemetry is not available or any error occurs, return unwrapped
        }
        return cf;
    }
}
