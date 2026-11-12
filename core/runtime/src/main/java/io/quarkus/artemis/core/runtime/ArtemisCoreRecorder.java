package io.quarkus.artemis.core.runtime;

import java.util.Objects;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

/**
 * Builds the Artemis core runtime objects.
 * <p>
 * The static methods of this class run from the services that the deployment module registers.
 */
public class ArtemisCoreRecorder {

    public static ServerLocator createServerLocator(String name, ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        return Objects.requireNonNull(getServerLocator(runtimeConfig));
    }

    protected static ServerLocator getServerLocator(ArtemisRuntimeConfig runtimeConfig) {
        if (runtimeConfig.url().isPresent()) {
            try {
                return applyConfig(ActiveMQClient.createServerLocator(runtimeConfig.url().get()),
                        runtimeConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private static ServerLocator applyConfig(ServerLocator locator, ArtemisRuntimeConfig runtimeConfig) {
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
    }

}
