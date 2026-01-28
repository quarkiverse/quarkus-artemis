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
            ActiveMQConnectionFactory cf;
            if (isXaEnabled) {
                cf = (ActiveMQConnectionFactory) wrapper.apply(new ActiveMQXAConnectionFactory(
                        url,
                        runtimeConfig.getUsername(),
                        runtimeConfig.getPassword()));
            } else {
                cf = (ActiveMQConnectionFactory) wrapper.apply(new ActiveMQConnectionFactory(
                        url,
                        runtimeConfig.getUsername(),
                        runtimeConfig.getPassword()));
            }

            // --- Flow Control ---
            runtimeConfig.consumerWindowSize().ifPresent(cf::setConsumerWindowSize);
            runtimeConfig.consumerMaxRate().ifPresent(cf::setConsumerMaxRate);
            runtimeConfig.producerWindowSize().ifPresent(cf::setProducerWindowSize);
            runtimeConfig.producerMaxRate().ifPresent(cf::setProducerMaxRate);
            runtimeConfig.confirmationWindowSize().ifPresent(cf::setConfirmationWindowSize);
            runtimeConfig.minLargeMessageSize().ifPresent(cf::setMinLargeMessageSize);
            runtimeConfig.compressLargeMessage().ifPresent(cf::setCompressLargeMessage);
            runtimeConfig.compressionLevel().ifPresent(cf::setCompressionLevel);
            runtimeConfig.cacheLargeMessagesClient().ifPresent(cf::setCacheLargeMessagesClient);

            // --- Connectivity & Timeouts ---
            runtimeConfig.clientFailureCheckPeriod().ifPresent(cf::setClientFailureCheckPeriod);
            runtimeConfig.connectionTtl().ifPresent(cf::setConnectionTTL);
            runtimeConfig.callTimeout().ifPresent(cf::setCallTimeout);
            runtimeConfig.callFailoverTimeout().ifPresent(cf::setCallFailoverTimeout);
            runtimeConfig.useGlobalPools().ifPresent(cf::setUseGlobalPools);
            runtimeConfig.scheduledThreadPoolMaxSize().ifPresent(cf::setScheduledThreadPoolMaxSize);
            runtimeConfig.threadPoolMaxSize().ifPresent(cf::setThreadPoolMaxSize);

            // --- Retries & Failover ---
            runtimeConfig.reconnectAttempts().ifPresent(cf::setReconnectAttempts);
            runtimeConfig.initialConnectAttempts().ifPresent(cf::setInitialConnectAttempts);
            runtimeConfig.retryInterval().ifPresent(cf::setRetryInterval);
            runtimeConfig.retryIntervalMultiplier().ifPresent(cf::setRetryIntervalMultiplier);
            runtimeConfig.maxRetryInterval().ifPresent(cf::setMaxRetryInterval);
            runtimeConfig.failoverOnInitialConnection().ifPresent(cf::setFailoverOnInitialConnection);

            // --- Behavior & Grouping ---
            runtimeConfig.autoGroup().ifPresent(cf::setAutoGroup);
            runtimeConfig.groupID().ifPresent(cf::setGroupID);
            runtimeConfig.blockOnAcknowledge().ifPresent(cf::setBlockOnAcknowledge);
            runtimeConfig.blockOnDurableSend().ifPresent(cf::setBlockOnDurableSend);
            runtimeConfig.blockOnNonDurableSend().ifPresent(cf::setBlockOnNonDurableSend);
            runtimeConfig.preAcknowledge().ifPresent(cf::setPreAcknowledge);
            runtimeConfig.dupsOKBatchSize().ifPresent(cf::setDupsOKBatchSize);
            runtimeConfig.transactionBatchSize().ifPresent(cf::setTransactionBatchSize);
            runtimeConfig.cacheDestinations().ifPresent(cf::setCacheDestinations);

            // --- Security & Policy ---
            runtimeConfig.deserializationWhiteList().ifPresent(cf::setDeserializationWhiteList);
            runtimeConfig.deserializationBlackList().ifPresent(cf::setDeserializationBlackList);
            runtimeConfig.incomingInterceptorList().ifPresent(cf::setIncomingInterceptorList);
            runtimeConfig.outgoingInterceptorList().ifPresent(cf::setOutgoingInterceptorList);
            runtimeConfig.connectionLoadBalancingPolicyClassName().ifPresent(cf::setConnectionLoadBalancingPolicyClassName);
            runtimeConfig.protocolManagerFactoryStr().ifPresent(cf::setProtocolManagerFactoryStr);

            // --- Compatibility & Identification ---
            runtimeConfig.clientID().ifPresent(cf::setClientID);
            runtimeConfig.enableSharedClientID().ifPresent(cf::setEnableSharedClientID);
            runtimeConfig.enable1xPrefixes().ifPresent(cf::setEnable1xPrefixes);
            runtimeConfig.ignoreJTA().ifPresent(cf::setIgnoreJTA);
            runtimeConfig.useTopologyForLoadBalancing().ifPresent(cf::setUseTopologyForLoadBalancing);
            runtimeConfig.initialMessagePacketSize().ifPresent(cf::setInitialMessagePacketSize);

            return cf;
        } else {
            return null;
        }
    }
}
