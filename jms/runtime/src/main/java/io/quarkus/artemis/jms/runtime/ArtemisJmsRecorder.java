package io.quarkus.artemis.jms.runtime;

import java.util.Objects;

import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;

/**
 * Builds the Artemis JMS runtime objects.
 * <p>
 * The static methods of this class run from the services that the deployment module registers.
 */
public class ArtemisJmsRecorder {

    public static ConnectionFactoryWrapper getDefaultWrapper() {
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
    public static ConnectionFactoryWrapper composeWrappers(
            ConnectionFactoryWrapper wrapper1,
            ConnectionFactoryWrapper wrapper2) {
        return cf -> {
            Object wrapped = wrapper1.apply(cf);
            if (wrapped instanceof ConnectionFactory connectionFactory) {
                return wrapper2.apply(connectionFactory);
            }
            return wrapped;
        };
    }

    public static ConnectionFactory createConnectionFactory(String name, ArtemisRuntimeConfigs runtimeConfigs,
            ArtemisBuildTimeConfigs buildTimeConfigs, ConnectionFactoryWrapper wrapper) {
        ArtemisRuntimeConfig runtimeConfig = runtimeConfigs.configs().get(name);
        ArtemisBuildTimeConfig buildTimeConfig = buildTimeConfigs.configs().get(name);
        ArtemisUtil.validateIntegrity(name, runtimeConfig, buildTimeConfig);
        return Objects.requireNonNull(extractConnectionFactory(buildTimeConfig.isXaEnabled(), runtimeConfig, wrapper));
    }

    private static ConnectionFactory extractConnectionFactory(boolean isXaEnabled, ArtemisRuntimeConfig runtimeConfig,
            ConnectionFactoryWrapper wrapper) {
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

}
