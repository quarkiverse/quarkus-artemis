package io.quarkus.artemis.core.runtime;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisCoreRecorder {
    private final ArtemisBuildTimeConfigs buildTimeConfigs;
    private final RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs;

    public ArtemisCoreRecorder(ArtemisBuildTimeConfigs buildTimeConfigs, RuntimeValue<ArtemisRuntimeConfigs> runtimeConfigs) {
        this.buildTimeConfigs = buildTimeConfigs;
        this.runtimeConfigs = runtimeConfigs;
    }

    public Supplier<ServerLocator> getServerLocatorSupplier(String name) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.getValue().configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        ServerLocator serverLocator = Objects.requireNonNull(getServerLocator(runtimeConfig));
        return () -> serverLocator;
    }

    protected static ServerLocator getServerLocator(ArtemisRuntimeConfig runtimeConfig) {
        if (runtimeConfig.url().isPresent()) {
            try {
                ServerLocator locator = ActiveMQClient.createServerLocator(runtimeConfig.url().get());

                // --- Flow Control ---
                runtimeConfig.consumerWindowSize().ifPresent(locator::setConsumerWindowSize);
                runtimeConfig.consumerMaxRate().ifPresent(locator::setConsumerMaxRate);
                runtimeConfig.producerWindowSize().ifPresent(locator::setProducerWindowSize);
                runtimeConfig.producerMaxRate().ifPresent(locator::setProducerMaxRate);
                runtimeConfig.confirmationWindowSize().ifPresent(locator::setConfirmationWindowSize);
                runtimeConfig.minLargeMessageSize().ifPresent(locator::setMinLargeMessageSize);
                runtimeConfig.compressLargeMessage().ifPresent(locator::setCompressLargeMessage);
                runtimeConfig.compressionLevel().ifPresent(locator::setCompressionLevel);
                runtimeConfig.cacheLargeMessagesClient().ifPresent(locator::setCacheLargeMessagesClient);

                // --- Connectivity & Timeouts ---
                runtimeConfig.clientFailureCheckPeriod().ifPresent(locator::setClientFailureCheckPeriod);
                runtimeConfig.connectionTtl().ifPresent(locator::setConnectionTTL);
                runtimeConfig.callTimeout().ifPresent(locator::setCallTimeout);
                runtimeConfig.callFailoverTimeout().ifPresent(locator::setCallFailoverTimeout);
                runtimeConfig.useGlobalPools().ifPresent(locator::setUseGlobalPools);
                runtimeConfig.scheduledThreadPoolMaxSize().ifPresent(locator::setScheduledThreadPoolMaxSize);
                runtimeConfig.threadPoolMaxSize().ifPresent(locator::setThreadPoolMaxSize);

                // --- Retries & Failover ---
                runtimeConfig.reconnectAttempts().ifPresent(locator::setReconnectAttempts);
                runtimeConfig.initialConnectAttempts().ifPresent(locator::setInitialConnectAttempts);
                runtimeConfig.failoverAttempts().ifPresent(locator::setFailoverAttempts);
                runtimeConfig.retryInterval().ifPresent(locator::setRetryInterval);
                runtimeConfig.retryIntervalMultiplier().ifPresent(locator::setRetryIntervalMultiplier);
                runtimeConfig.maxRetryInterval().ifPresent(locator::setMaxRetryInterval);
                runtimeConfig.failoverOnInitialConnection().ifPresent(locator::setFailoverOnInitialConnection);

                // --- Behavior & Grouping ---
                runtimeConfig.autoGroup().ifPresent(locator::setAutoGroup);
                runtimeConfig.groupID().ifPresent(locator::setGroupID);
                runtimeConfig.blockOnAcknowledge().ifPresent(locator::setBlockOnAcknowledge);
                runtimeConfig.blockOnDurableSend().ifPresent(locator::setBlockOnDurableSend);
                runtimeConfig.blockOnNonDurableSend().ifPresent(locator::setBlockOnNonDurableSend);
                runtimeConfig.preAcknowledge().ifPresent(locator::setPreAcknowledge);
                runtimeConfig.initialMessagePacketSize().ifPresent(locator::setInitialMessagePacketSize);

                // --- Core Specific Section ---
                runtimeConfig.ackBatchSize().ifPresent(locator::setAckBatchSize);
                runtimeConfig.onMessageCloseTimeout().ifPresent(locator::setOnMessageCloseTimeout);
                runtimeConfig.useTopologyForLoadBalancing().ifPresent(locator::setUseTopologyForLoadBalancing);

                // --- Security & Policy ---
                runtimeConfig.incomingInterceptorList().ifPresent(locator::setIncomingInterceptorList);
                runtimeConfig.outgoingInterceptorList().ifPresent(locator::setOutgoingInterceptorList);
                runtimeConfig.connectionLoadBalancingPolicyClassName()
                        .ifPresent(locator::setConnectionLoadBalancingPolicyClassName);

                return locator;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}
