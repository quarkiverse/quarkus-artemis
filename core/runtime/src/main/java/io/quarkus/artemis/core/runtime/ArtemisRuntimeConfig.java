package io.quarkus.artemis.core.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface ArtemisRuntimeConfig {
    /**
     * Artemis connection url.
     */
    Optional<String> url();

    /**
     * Username for authentication, only used with JMS.
     */
    Optional<String> username();

    /**
     * Password for authentication, only used with JMS.
     */
    Optional<String> password();

    /**
     * Whether this particular data source should be excluded from the health check if
     * the general health check for data sources is enabled.
     * <p>
     * By default, the health check includes all configured data sources (if it is enabled).
     */
    Optional<Boolean> healthExclude();

    // --- Common Flow Control & Buffering ---

    /**
     * The window size for consumer flow control in bytes.
     */
    Optional<Integer> consumerWindowSize();

    /**
     * The maximum rate of messages consumed per second.
     */
    Optional<Integer> consumerMaxRate();

    /**
     * The window size for producer flow control in bytes.
     */
    Optional<Integer> producerWindowSize();

    /**
     * The maximum rate of messages produced per second.
     */
    Optional<Integer> producerMaxRate();

    /**
     * The window size for confirmation of sent messages in bytes.
     */
    Optional<Integer> confirmationWindowSize();

    /**
     * The threshold in bytes for treating a message as a large message.
     */
    Optional<Integer> minLargeMessageSize();

    /**
     * Whether to compress large messages sent to the server.
     */
    Optional<Boolean> compressLargeMessage();

    /**
     * The compression level to use (0-9).
     */
    Optional<Integer> compressionLevel();

    /**
     * Whether to cache large messages on the client side.
     */
    Optional<Boolean> cacheLargeMessagesClient();

    // --- Common Connectivity & Timeouts ---

    /**
     * The frequency (ms) to check for client failure.
     */
    Optional<Long> clientFailureCheckPeriod();

    /**
     * How long (ms) to keep a connection alive without receiving a packet.
     */
    Optional<Long> connectionTtl();

    /**
     * Total time (ms) to wait for a blocking call to complete.
     */
    Optional<Long> callTimeout();

    /**
     * Total time (ms) to wait for a call during failover.
     */
    Optional<Long> callFailoverTimeout();

    /**
     * Whether to use global thread pools.
     */
    Optional<Boolean> useGlobalPools();

    /**
     * The maximum size of the scheduled thread pool.
     */
    Optional<Integer> scheduledThreadPoolMaxSize();

    /**
     * The maximum size of the general purpose thread pool.
     */
    Optional<Integer> threadPoolMaxSize();

    // --- Common Retries & Failover ---

    /**
     * Maximum number of reconnection attempts. Use -1 for infinite.
     */
    Optional<Integer> reconnectAttempts();

    /**
     * Reconnection attempts for the initial connection.
     */
    Optional<Integer> initialConnectAttempts();

    /**
     * Maximum number of failover attempts.
     */
    Optional<Integer> failoverAttempts();

    /**
     * Interval (ms) between reconnection attempts.
     */
    Optional<Long> retryInterval();

    /**
     * Multiplier for exponential backoff on retries.
     */
    Optional<Double> retryIntervalMultiplier();

    /**
     * Maximum interval (ms) between retries.
     */
    Optional<Long> maxRetryInterval();

    /**
     * Whether to failover to backup on initial connection.
     */
    Optional<Boolean> failoverOnInitialConnection();

    // --- Common Behavior & Grouping ---

    /**
     * Whether to automatically group messages.
     */
    Optional<Boolean> autoGroup();

    /**
     * The group ID to use for messages.
     */
    Optional<String> groupID();

    /**
     * Whether consumers block while sending acknowledgments.
     */
    Optional<Boolean> blockOnAcknowledge();

    /**
     * Whether producers block when sending durable messages.
     */
    Optional<Boolean> blockOnDurableSend();

    /**
     * Whether producers block when sending non-durable messages.
     */
    Optional<Boolean> blockOnNonDurableSend();

    /**
     * Whether messages are pre-acknowledged on the server.
     */
    Optional<Boolean> preAcknowledge();

    /**
     * The initial size of message packets in bytes.
     */
    Optional<Integer> initialMessagePacketSize();

    // --- Core Specific Section ---

    /**
     * Core Only: The acknowledgments batch size.
     */
    Optional<Integer> ackBatchSize();

    /**
     * Core Only: Timeout (ms) for onMessage completion when closing consumers.
     */
    Optional<Integer> onMessageCloseTimeout();

    /**
     * Core Only: Whether to use cluster topology for load balancing.
     */
    Optional<Boolean> useTopologyForLoadBalancing();

    // --- JMS Specific Section ---

    /**
     * JMS Only: The client ID for the connection.
     */
    Optional<String> clientID();

    /**
     * JMS Only: Whether multiple connections can share the same client ID.
     */
    Optional<Boolean> enableSharedClientID();

    /**
     * JMS Only: Whether to use 1.x message prefixes (jms.queue.).
     */
    Optional<Boolean> enable1xPrefixes();

    /**
     * JMS Only: Whether to ignore JTA for session management.
     */
    Optional<Boolean> ignoreJTA();

    /**
     * JMS Only: The batch size for DUPS_OK_ACKNOWLEDGE mode.
     */
    Optional<Integer> dupsOKBatchSize();

    /**
     * JMS Only: The batch size for transacted sessions in bytes.
     */
    Optional<Integer> transactionBatchSize();

    /**
     * JMS Only: Whether to cache JMS destinations on the client.
     */
    Optional<Boolean> cacheDestinations();

    /**
     * JMS Only: Allowed classes for ObjectMessage deserialization.
     */
    Optional<String> deserializationAllowList();

    /**
     * JMS Only: Forbidden classes for ObjectMessage deserialization.
     */
    Optional<String> deserializationDenyList();

    // --- Interceptors & Policies ---

    /**
     * Comma-separated list of incoming interceptors.
     */
    Optional<String> incomingInterceptorList();

    /**
     * Comma-separated list of outgoing interceptors.
     */
    Optional<String> outgoingInterceptorList();

    /**
     * Class name of the connection load balancing policy.
     */
    Optional<String> connectionLoadBalancingPolicyClassName();

    /**
     * Class name of the protocol manager factory.
     */
    Optional<String> protocolManagerFactoryStr();

    default String getUrl() {
        return url().orElse(null);
    }

    default String getUsername() {
        return username().orElse(null);
    }

    default String getPassword() {
        return password().orElse(null);
    }

    default boolean isHealthExclude() {
        return healthExclude().orElse(false);
    }

    default boolean isHealthInclude() {
        return !isHealthExclude();
    }

    default boolean isEmpty() {
        return url().isEmpty() && username().isEmpty() && password().isEmpty() && healthExclude().isEmpty();
    }

    default boolean isPresent() {
        return !isEmpty();
    }
}
